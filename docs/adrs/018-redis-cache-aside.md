# ADR-018: Redis Cache-Aside (ADR-018)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Introduce Redis as a cache-aside store for read-heavy data to reduce PostgreSQL load.

**Context:** The tadka-api monolith hits PostgreSQL on every read for menu items, orders, and coupons. During peak dinner rush, the database becomes the bottleneck. A cache-aside pattern lets us serve frequent reads from Redis while keeping PostgreSQL as the source of truth.

**Options:**
1. **Write-through cache:** Every write updates both Redis and PostgreSQL synchronously.
2. **Cache-aside (lazy loading):** Application checks Redis first; on miss, reads from PostgreSQL and populates Redis.
3. **Read-through:** A caching layer sits between the repository and PostgreSQL.

**Choice:** Option 2 — Cache-aside. Application code explicitly manages the cache: `cacheService.get(key, () -> db.find())`. Redis is the read-through layer; PostgreSQL remains the authoritative write destination.

**Why:** Write-through adds latency to every write. Read-through requires a custom proxy layer. Cache-aside is the simplest pattern that gives us control over cache invalidation, TTL, and fallback behavior. We can swap `RedisCacheService` ↔ `InMemoryFallbackCacheService` ↔ `NullCacheService` via `cache.mode`.

**Trade-off:** Application code must be cache-aware (explicit get/put/evict). Stale data is possible if invalidation is missed. **At-most-once:** a crashed process loses cached data; the DB always has the truth.

**Failure mode:** Redis is unreachable → `DataAccessException` → graceful fallback to `NullCacheService` (all reads hit DB). The app stays up; just slower.

**Revisit when:** We need sub-millisecond reads at massive scale. Then consider a write-behind cache or a dedicated read-replica with query caching.

**References:**
- ADR-019: Cache stampede protection
- Day 6 runbook: `docs/runbooks/day-06.md`
