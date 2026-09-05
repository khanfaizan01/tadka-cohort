# ADR-008: No cross-schema foreign keys

**Date:** 2026-08-28

**Status:** Accepted

**Deciders:** Architecture team / Tadka cohort

## Context

ADR-003 introduced schema-per-domain isolation within a single PostgreSQL 16 instance. While schemas provide namespace separation, PostgreSQL allows foreign keys that span schemas. Allowing cross-schema FKs would create physical database-level coupling between bounded contexts, undermining the domain boundary that schemas were meant to enforce.

## Decision

**No cross-schema foreign keys.** Cross-domain references use UUID identifiers only, validated in application code. Within-schema foreign keys are allowed and encouraged to enforce referential integrity inside a single domain.

For example:

- `ordering.orders.restaurant_id` is a `UUID` referencing `restaurant.restaurants.id` — **enforced in application code**, not by a database FK.
- `ordering.orders.customer_id` is a `UUID` referencing `identity.users.id` — **enforced in application code**.
- `ordering.order_items.order_id` references `ordering.orders.id` via a true FK — **allowed**, same schema.

## Consequences

### Positive

- Preserves domain autonomy: each schema can evolve independently
- Prevents cascade deletes and constraint chain reactions across domains
- Forces the application to own data integrity, making domain logic explicit and testable
- Aligns with the microservice ideal of database-per-service, even within a monolith
- Enables future extraction of a domain into its own service without migration nightmares

### Negative

- No database-level referential guarantee for cross-domain relationships
- Orphaned records are possible if application validation has bugs
- Application code must implement join-like logic or event-driven consistency
- Slightly more boilerplate in the service/application layer

### Risks

- A developer might be tempted to create a cross-schema FK "just to be safe"
- Data integrity bugs may go unnoticed until they surface in production
- Testing must cover cross-domain reference validation explicitly

## Alternatives Considered

### Option A: Allow cross-schema foreign keys

- **Pros:** Full database referential integrity, no orphaned records, simpler application code
- **Cons:** Creates physical coupling between schemas, makes extraction into microservices harder, cascade risks
- **Why rejected:** Directly contradicts the domain boundary purpose of ADR-003. Database-level coupling is the problem schema-per-domain was meant to solve.

### Option B: Use a shared "cross-domain" schema for reference tables

- **Pros:** Centralizes shared entities, avoids cross-schema FKs in domain schemas
- **Cons:** Creates a new coupling point (the shared schema), ambiguity about ownership
- **Why rejected:** The shared schema becomes a hidden dependency; UUID references in application code are a cleaner, more explicit contract.

## References

- ADR-003: Schema-per-domain in single Postgres DB
- ADR-002: Monolith-first architecture

## Revisit When

- The team extracts a domain into its own microservice and needs database-level consistency during the transition
- A domain's data volume justifies independent database deployment
- A formal data-integrity audit flags cross-domain orphaning as an unacceptable risk
