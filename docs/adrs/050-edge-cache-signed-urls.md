# ADR-050: Edge Cache Signed URLs (ADR-050)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Allow edge caches/CDNs to cache API responses via signed URLs without exposing sensitive data.

**Context:** API responses (invoices, menus) can be cached at the edge (CDN) to reduce origin load. But standard URL caching is dangerous — anyone can request any URL and get the response. Signed URLs use HMAC-SHA256 to authenticate requests, allowing edge caches to serve responses without exposing data to unauthorized clients.

**Options:**
1. **No edge caching:** All requests hit the origin.
2. **Public edge caching:** Cache all responses at the edge (vulnerable to abuse).
3. **Signed URLs:** Generate HMAC-signed URLs that expire after a configurable TTL. Only clients with the correct signature can access cached responses.

**Choice:** Option 3 — Signed URLs. `UrlSigner` generates HMAC-SHA256 signatures. The `OrderInvoiceController` returns signed URLs for invoices. Edge caches can serve the response without re-visiting the origin until the signature expires.

**Why:** Signed URLs are the standard pattern for secure edge caching. They're simple to implement (HMAC + timestamp), well-supported by CDNs, and don't require session state. The `SignedInvoiceUrlResponse` returns the signed URL to the client.

**Trade-off:** URL length increases (signature + timestamp). URL expiration requires clients to re-request when the signature expires. **Security:** the signing key must be kept secret.

**Failure mode:** Signing fails → returns unsigned URL (falls back to direct access). The API stays functional.

**Revisit when:** We need CDN-level caching with authentication. Then consider JWT-based tokens or OAuth2 tokens for edge authentication.

**References:**
- `UrlSigner.java`
- `OrderInvoiceController.java`
