package com.tadka.infrastructure.ratelimiting;

/**
 * Result of a rate limit check.
 */
public record RateLimitResult(boolean allowed, double retryAfterSeconds) {}
