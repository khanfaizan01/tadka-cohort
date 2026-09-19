package com.tadka.infrastructure.caching;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Cache-aside over Redis (ADR-018) with single-flight stampede protection (ADR-019).
 * On a miss, exactly one caller acquires a short SET NX EX lock and refreshes from the DB while
 * others briefly wait and re-read. Any Redis failure degrades gracefully to the DB factory —
 * Redis is a performance dependency, not a correctness one.
 */
public class RedisCacheService implements ICacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private final ObjectMapper mapper = new ObjectMapper();

    private final StringRedisTemplate redis;

    public RedisCacheService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public <T> T getOrSet(String key, TypeReference<T> typeRef, Supplier<T> factory, Duration ttl) {
        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                return deserialize(cached, typeRef);
            }

            String lockKey = "lock:" + key;
            String token = UUID.randomUUID().toString().replace("-", "");
            Boolean isRefresher = redis.opsForValue().setIfAbsent(lockKey, token, LOCK_TTL.toSeconds(), TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(isRefresher)) {
                try {
                    T value = factory.get();
                    if (value != null) {
                        redis.opsForValue().set(key, serialize(value), ttl.toSeconds(), TimeUnit.SECONDS);
                    }
                    return value;
                } finally {
                    String owner = redis.opsForValue().get(lockKey);
                    if (token.equals(owner)) {
                        redis.delete(lockKey);
                    }
                }
            }

            for (int attempt = 0; attempt < 5; attempt++) {
                try { Thread.sleep(80); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                cached = redis.opsForValue().get(key);
                if (cached != null) return deserialize(cached, typeRef);
            }
            return factory.get();
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable for key {}; falling back to the database.", key);
            return factory.get();
        }
    }

    @Override
    public void invalidate(String key) {
        try {
            redis.delete(key);
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable invalidating key {}; TTL will bound staleness.", key);
        }
    }

    private <T> String serialize(T value) {
        try { return mapper.writeValueAsString(value); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }

    private <T> T deserialize(String json, TypeReference<T> typeRef) {
        try { return mapper.readValue(json, typeRef); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}
