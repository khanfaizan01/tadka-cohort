# ADR-049: Distributed Rate Limiting (ADR-049)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Implement distributed rate limiting to protect the API from abuse across multiple instances.

**Context:** Without rate limiting, a single client can overwhelm the API with requests. Per-instance rate limiting fails when we scale horizontally — a client can distribute requests across instances to bypass limits.

**Options:**
1. **IP whitelist:** Only allow known IPs.
2. **Per-instance rate limiting:** Each instance limits independently.
3. **Distributed rate limiting via Redis:** All instances share a centralized counter in Redis.
4. **API gateway rate limiting:** Offload rate limiting to the gateway/load balancer.

**Choice:** Option 3 — Distributed rate limiting via Redis. `RedisFixedWindowRateLimiter` uses Redis INCR with TTL to count requests per IP across all instances. The `RateLimitingMiddleware` intercepts requests and checks the limit before processing.

**Why:** Redis-based distributed rate limiting is accurate across all instances. The fixed-window algorithm is simple and performant. Sliding window is available via configuration for more precise rate control.

**Trade-off:** Redis adds ~1ms latency per request for the rate-limit check. Redis is a dependency; if it fails, `NullRateLimiter` allows all requests (fail-open).

**Failure mode:** Redis down → `NullRateLimiter` → all requests pass (fail-open). The API stays up but is unprotected.

**Revisit when:** We need more sophisticated rate limiting (token bucket, adaptive limits). Then consider `RedisSlidingWindowRateLimiter` or a dedicated API gateway.

**References:**
- ADR-050: Edge cache signed URLs
- `toydemo/rate-limiter-toy/`
