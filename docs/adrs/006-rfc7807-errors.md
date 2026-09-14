# ADR-006: RFC 7807 Problem Details for Errors

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** What does the error response body look like when something goes wrong?

**Options:**
1. Plain-text or HTML error pages.
2. `{"error": "message"}` — ad-hoc JSON.
3. RFC 7807 `application/problem+json` (`type`, `title`, `status`, `detail`, `instance`).
4. Custom envelope with `code`, `message`, `data` fields.

**Choice:** Option 3. Spring `ResponseEntity` with `ProblemDetail` objects. A `@ControllerAdvice` / `GlobalExceptionHandler` maps every exception to the correct HTTP status and an RFC 7807 body.

**Why:** Clients (mobile, web) need a machine-readable, consistent error shape. RFC 7807 is the IETF standard. `status` is the HTTP code; `detail` is human-readable context; `type` points to documentation. Spring Boot 3.x ships `ProblemDetail` natively.

**Trade-off:** More verbose than a simple `{"error": "..."}`. Some clients may not understand `type`. The `violations` field (list of field-level validation errors) is a Spring Boot extension not in the strict RFC.

**Failure mode:** Returning 500 with an empty body exposes internal details to the client. Or a custom `{"code": 500, "message": "..."}` that requires every client to maintain a code-to-HTTP mapping.

**Revisit when:** A third-party API consumer requires a specific error envelope. Then an adapter layer translates RFC 7807 to the external format without changing our internal contract.
