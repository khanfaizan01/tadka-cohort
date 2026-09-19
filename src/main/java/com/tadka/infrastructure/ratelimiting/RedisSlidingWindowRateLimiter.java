package com.tadka.infrastructure.ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;

/**
 * Sliding window rate limiter via Redis sorted set (ADR-049).
 */
public class RedisSlidingWindowRateLimiter implements IRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisSlidingWindowRateLimiter.class);
    private final StringRedisTemplate redis;
    private final int limit;
    private final long windowMillis;

    private static final String SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local windowMs = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        local member = ARGV[4]
        redis.call('ZREMRANGEBYSCORE', key, 0, now - windowMs)
        local count = redis.call('ZCARD', key)
        if count < limit then
            redis.call('ZADD', key, now, member)
            redis.call('PEXPIRE', key, windowMs)
            return {1, -1}
        end
        local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
        local retryMs = windowMs
        if oldest[2] ~= nil then
            retryMs = windowMs - (now - tonumber(oldest[2]))
        end
        return {0, retryMs}
        """;

    public RedisSlidingWindowRateLimiter(StringRedisTemplate redis, int limit, long windowMillis) {
        this.redis = redis;
        this.limit = limit;
        this.windowMillis = windowMillis;
    }

    @Override
    public RateLimitResult checkRateLimit(String key) {
        try {
            String redisKey = "ratelimit:sliding:" + key;
            long now = System.currentTimeMillis();
            String member = now + "-" + java.util.UUID.randomUUID().toString().replace("-", "");

            DefaultRedisScript<List> script = new DefaultRedisScript<>(SCRIPT, List.class);
            List<?> result = redis.execute(script, List.of(redisKey),
                String.valueOf(now), String.valueOf(windowMillis), String.valueOf(limit), member);

            boolean allowed = ((Number) result.get(0)).longValue() == 1;
            double retryAfterMs = ((Number) result.get(1)).doubleValue();
            return new RateLimitResult(allowed, allowed ? 0 : Math.max(1, retryAfterMs / 1000.0));
        } catch (DataAccessException ex) {
            log.warn("Rate limiter Redis unavailable; failing open.");
            return new RateLimitResult(true, 0);
        }
    }
}
