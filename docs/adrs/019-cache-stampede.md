# ADR-019: Cache Stampede Protection (ADR-019)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Prevent cache stampedes when a popular cache key expires simultaneously across N requests.

**Context:** In a cache-aside system, if a hot key (e.g., "menu-today") expires and N concurrent requests all miss the cache, all N requests hit the database simultaneously. This is the "thundering herd" / "cache stampede" problem. The single request that refreshes the cache is fine; the N-1 others are wasted and can overwhelm the DB.

**Options:**
1. **Lock-based:** Use Redis `SET NX` to acquire a lock; only one request refreshes, others wait for the result.
2. **Probabilistic early expiration:** Refresh the cache slightly before it expires (background refresh).
3. **Lock-free with short TTL:** Accept the stampede; use very short TTLs so the impact is minimal.
4. **Futures/promise cache:** Store a "pending" future in the cache; concurrent requests wait on the same future.

**Choice:** Option 1 — Redis `SET NX` lock. The `cacheService` uses `SET NX` to acquire a lock key; only the winning request calls `fetchFromDB()`. Losing requests poll for the result or fall back to a short TTL miss.

**Why:** `SET NX` is atomic and simple. It requires no background refresh logic and no custom promise caching. The lock key has a short TTL (5s) to prevent deadlocks if the winning request crashes.

**Trade-off:** Lock overhead per cache miss. Poll-back requests add latency to losing requests. **Deadlock risk:** if the winning request crashes before releasing the lock, the lock TTL auto-expires (safety net).

**Failure mode:** Redis is down → no lock → falls back to `NullCacheService` → all requests hit DB (degraded mode, but no crash).

**Revisit when:** We see high cache-miss rates on hot keys. Then consider background refresh (Option 2) or a probabilistic early-expiration strategy.

**References:**
- ADR-018: Redis cache-aside
- `toydemo/hot-key-stampede-toy/real-redis.js`
