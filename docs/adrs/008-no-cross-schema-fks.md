# ADR-008: No cross-schema foreign keys

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Should we allow foreign keys that span schema boundaries in PostgreSQL?

**Options:**
1. Allow cross-schema foreign keys (full database referential integrity).
2. Use a shared "cross-domain" schema for reference tables.
3. No cross-schema foreign keys — UUID references validated in application code.

**Choice:** Option 3. No cross-schema FKs. Within-schema FKs are allowed and encouraged. Cross-domain references use UUID identifiers only, validated in application code.

**Why:** Cross-schema FKs create physical coupling between bounded contexts, undermining the domain boundary that schemas were meant to enforce. They also make future extraction into microservices harder (cascade risks, migration nightmares). Application code owning data integrity makes domain logic explicit and testable.

**Trade-off:** No database-level referential guarantee for cross-domain relationships. Orphaned records are possible if application validation has bugs. Slightly more boilerplate in the service/application layer. A developer might be tempted to create a cross-schema FK "just to be safe."

**Failure mode:** A developer creates a cross-schema FK bypassing the ADR. Data integrity bugs go unnoticed until they surface in production. Or extraction into microservices hits a wall of cascade delete chains.

**Revisit when:** The team extracts a domain into its own microservice and needs database-level consistency during the transition; a domain's data volume justifies independent database deployment; a formal data-integrity audit flags cross-domain orphaning as an unacceptable risk.
