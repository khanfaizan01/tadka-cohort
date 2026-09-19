package com.tadka.infrastructure.ratelimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Fixed window rate limiter via Redis sorted set (ADR-049).
 */
public class RedisFixedWindowRateLimiter implements IRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisFixedWindowRateLimiter.class);
    private final StringRedisTemplate redis;
    private final int limit;
    private final long windowSeconds;

    public RedisFixedWindowRateLimiter(StringRedisTemplate redis, int limit, long windowSeconds) {
        this.redis = redis;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    @Override
    public RateLimitResult checkRateLimit(String key) {
        try {
            String redisKey = "ratelimit:fixed:" + key;
            long now = System.currentTimeMillis() / 1000;
            long windowStart = now - windowSeconds;

            redis.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);
            Set<String> members = redis.opsForZSet().range(redisKey, 0, -1);
            long count = members != null ? members.size() : 0;

            if (count < limit) {
                redis.opsForZSet().add(redisKey, String.valueOf(now), now);
                redis.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
                return new RateLimitResult(true, 0);
            }

            double oldestScore = members != null && !members.isEmpty()
                ? redis.opsForZSet().score(redisKey, members.iterator().next()).doubleValue()
                : now;
            double retryAfter = windowSeconds - (now - oldestScore);
            return new RateLimitResult(false, Math.max(1, retryAfter));
        } catch (DataAccessException ex) {
            log.warn("Rate limiter Redis unavailable; failing open.");
            return new RateLimitResult(true, 0);
        }
    }
}
