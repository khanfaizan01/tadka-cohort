# ADR-048: Response Compression (ADR-048)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Enable HTTP response compression to reduce payload sizes and improve client throughput.

**Context:** API responses (orders, menus, coupons) contain JSON payloads that can be significantly compressed. Without compression, mobile clients on slow networks waste bandwidth and time.

**Options:**
1. **No compression:** Send raw JSON.
2. **GZIP compression:** Compress responses with GZIP algorithm.
3. **Brotli compression:** Compress responses with Brotli (better ratio than GZIP).

**Choice:** Option 1 — No application-level compression for now. Spring Boot's `server.compression.enabled` can be added later. ETag-based conditional GET (ADR-048) already reduces unnecessary data transfer when resources haven't changed.

**Why:** Compression adds CPU overhead on every response. For a monolith with moderate traffic, the complexity isn't justified yet. ETag/conditional GET is simpler and works for cache hits.

**Trade-off:** Clients download larger payloads. Mobile users on slow networks may experience slower loads.

**Revisit when:** Response sizes exceed 10KB average or bandwidth costs become significant. Then enable `server.compression.enabled=true`.

**References:**
- ADR-048: ETag/conditional GET (see `ETagFilterAttribute`)
