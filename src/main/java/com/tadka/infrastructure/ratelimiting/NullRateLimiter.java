package com.tadka.infrastructure.ratelimiting;

/**
 * No-op rate limiter — used when Redis is not configured. All requests pass through.
 */
public class NullRateLimiter implements IRateLimiter {

    @Override
    public RateLimitResult checkRateLimit(String key) {
        return new RateLimitResult(true, 0);
    }
}
