# ADR-047: Stateless Scale-Out (ADR-047)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Ensure the tadka-api is stateless so it can scale horizontally behind a load balancer.

**Context:** The application stores session state in-memory (Tomcat sessions, in-memory caches). When we scale to multiple instances behind a load balancer, sticky sessions are required, and cache/rate-limiting state is not shared.

**Options:**
1. **Sticky sessions + shared nothing:** Each instance has its own state; load balancer routes by session ID.
2. **Stateless + Redis:** All state (cache, rate limits, tracking) lives in Redis; any instance can serve any request.
3. **Stateful with sticky sessions + session replication:** Tomcat session replication across instances.

**Choice:** Option 2 — Stateless with Redis. Cache-aside (`ICacheService`), rate limiting (`IRateLimiter`), and SSE tracking (`IOrderTrackingBus`) all delegate to Redis. No instance-local state. Any instance can handle any request.

**Why:** Stateless services are trivially horizontally scalable. Load balancer can use round-robin. Redis provides the shared state layer. No sticky session complexity.

**Trade-off:** Redis becomes a single point of failure and latency bottleneck. If Redis goes down, the app degrades to `NullCacheService`/`NullRateLimiter`/`NullOrderTrackingBus`. Redis latency adds ~1ms per cache/rate-limit check.

**Failure mode:** Redis down → graceful degradation to null implementations. App stays up but loses caching/rate-limiting/tracking benefits.

**Revisit when:** We need sub-millisecond latency or multi-region deployment. Then consider read replicas, Redis Cluster, or edge caching.

**References:**
- ADR-018: Redis cache-aside
- ADR-049: Distributed rate limiting
