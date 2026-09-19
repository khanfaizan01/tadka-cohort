package com.tadka.infrastructure.ratelimiting;

/**
 * Rate limiter contract (ADR-049). Redis down: fail OPEN (allow the request).
 * Rate limiting is leftover on this branch, not Sunday lecture; it must not
 * turn the taught "menu 200 when Redis dies" beat into a 500.
 */
public interface IRateLimiter {

    RateLimitResult checkRateLimit(String key);
}
