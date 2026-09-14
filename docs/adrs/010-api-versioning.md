# ADR-010: API Versioning via URL Path

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** How do we version the REST API so old clients keep working when we add features?

**Options:**
1. URL path versioning (`/api/v1/orders`).
2. Header versioning (`Accept: application/vnd.tadka.v1+json`).
3. Query parameter versioning (`/api/orders?version=1`).
4. Header-based via `Accept` (media type).
5. No versioning — just always serve the latest.

**Choice:** Option 1. All endpoints live under `/api/v1/...`. Controllers are namespaced `v1` for now. A future `/api/v2/` would be a new controller package, coexisting with v1 until migration is complete.

**Why:** URL path versioning is the most visible, easiest to debug (curl works), and most widely understood. Headers are invisible to curl/Postman users. Query parameters pollute logs and caches. The path is a clear contract boundary.

**Trade-off:** URLs are less "RESTful" (the URI identifies a version, not just a resource). Multiple versions in production increases testing surface. URL rewriting or redirect can be confusing. But for a cohort project, simplicity wins.

**Failure mode:** No versioning means every change breaks some client. Header versioning means a missing header gets a 406 or default version — not obvious from curl. Query parameters can be cached by CDNs incorrectly.

**Revisit when:** The API surface stabilizes and version negotiation becomes a real concern (multiple mobile app versions in the wild). Then header versioning or a version-agnostic approach with backward-compatible changes is viable.
