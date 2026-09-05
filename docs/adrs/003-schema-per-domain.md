# ADR-003: Schema-per-domain in single Postgres DB

**Date:** 2026-08-28

**Status:** Accepted

**Deciders:** Architecture team / Tadka cohort

## Context

Tadka uses a single PostgreSQL 16 database (ADR-002: monolith-first). The domain is decomposed into bounded contexts: ordering, restaurant, delivery, identity, and payment. A shared database with a single flat schema would blur domain boundaries and create unintended coupling between contexts.

## Decision

Use **schema-per-domain** within a single PostgreSQL 16 instance. Each bounded context gets its own schema:

- `ordering` — orders, order items, status history
- `restaurant` — restaurants, menus, items
- `delivery` — deliveries, drivers, tracking
- `identity` — users, roles, authentication
- `payment` — transactions, refunds, payment methods

JPA entities use `@Table(schema = "...")` to bind to the correct schema.

**No cross-schema foreign keys** are allowed (see ADR-008). Cross-domain references use UUID identifiers only, validated in application code.

## Consequences

### Positive

- Clear domain boundaries at the database level
- Organizes tables, indexes, and permissions by context
- Prevents accidental cross-domain table references through SQL
- Enables per-schema migration scripts with Flyway
- Simplifies permission management (future per-schema roles)

### Negative

- All schemas share one database instance — no physical isolation
- Cross-domain queries require joining across schemas, which is disallowed by design (ADR-008)
- Increased cognitive overhead for developers unfamiliar with PostgreSQL schemas
- Flyway migration management becomes more complex with multiple schemas

### Risks

- Schema name collisions if naming conventions are not enforced
- Developers may accidentally create tables in the wrong schema
- Cross-schema queries may be attempted via raw SQL, bypassing the ADR-008 contract

## Alternatives Considered

### Option A: Single flat schema for all domains

- **Pros:** Simplest setup, no schema prefixing needed, straightforward joins
- **Cons:** Blurred domain boundaries, risk of cross-domain table coupling, harder to reason about ownership
- **Why rejected:** Defeats the purpose of bounded contexts; a flat schema leads to a "big ball of mud" as the codebase grows.

### Option B: Separate database per domain (one DB per service)

- **Pros:** Complete physical isolation, independent scaling, strict data boundaries
- **Cons:** Requires distributed transactions for cross-domain operations, significant operational overhead, multiple connection pools
- **Why rejected:** Contradicts ADR-002 (monolith-first). Premature data isolation adds complexity the small team does not need.

### Option C: Single schema with table name prefixes

- **Pros:** No schema setup overhead, simple naming convention (e.g., `ordering_orders`)
- **Cons:** Prefixes are not enforced by the database, harder to manage permissions, joins can still cross prefixes freely
- **Why rejected:** Prefixes are a soft boundary at best; PostgreSQL schemas provide actual namespace isolation and permission control.

## References

- ADR-002: Monolith-first architecture
- ADR-008: No cross-schema foreign keys

## Revisit When

- A domain outgrows the shared instance and requires its own database server
- The team decides to decompose into microservices (each service gets its own DB)
- Cross-domain query performance becomes a measurable bottleneck
