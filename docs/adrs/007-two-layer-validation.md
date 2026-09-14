# ADR-007: Two-Layer Validation

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Where do we validate incoming requests — at the controller, the domain, or both?

**Options:**
1. Validate only at the controller (Spring `@Valid`, `@NotNull`, `@Pattern`).
2. Validate only in the domain (aggregate invariants).
3. Both layers: Spring validation for shape; domain aggregate for business invariants.
4. Validate in a separate service layer (anemic model).

**Choice:** Option 3. Spring `@Valid` with Jakarta annotations (`@NotNull`, `@Size`, `@Positive`) handles structural validation (empty items, null fields). The domain aggregate (`Order`) enforces business invariants (status transitions, cancellation reasons).

**Why:** Controller validation is fast and rejects malformed requests before hitting the database. Domain validation enforces rules the database cannot express (e.g., "cannot transition from CONFIRMED to DELIVERED directly"). Neither layer alone is sufficient.

**Trade-off:** Two validation layers means two places to write assertions. Controller validation uses reflection (`@Valid`) which has a small startup cost. Domain validation errors are `Result`/`ResultT` objects, not exceptions — a different error-handling pattern.

**Failure mode:** Validating only at the controller means business bugs reach the database. Validating only in the domain means garbage JSON reaches the aggregate, wasting resources. The `@ControllerAdvice` catches unhandled domain exceptions and maps them to 422 (domain violation) or 409 (concurrent modification).

**Revisit when:** A complex validation requires external data (e.g., "menu item must exist"). Then the domain calls a read repository, and validation crosses the layer boundary — that case is documented explicitly.
