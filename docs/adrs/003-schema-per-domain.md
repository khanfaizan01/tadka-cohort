# ADR-003: Schema-per-domain in single Postgres DB

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** How do we organize tables in one Postgres instance: one flat schema, or one schema per domain?

**Options:**
1. Single flat schema for all domains (one namespace).
2. Separate database per domain (one DB per service).
3. Schema-per-domain within a single PostgreSQL instance.
4. Single schema with table name prefixes (e.g., `ordering_orders`).

**Choice:** Option 3. Each bounded context gets its own schema (`ordering`, `restaurant`, `delivery`, `identity`, `payment`). JPA `@Table(schema = "...")` binds entities.

**Why:** Clear domain boundaries at the database level without the operational overhead of multiple databases. PostgreSQL schemas provide actual namespace isolation and permission control — prefixes are a soft boundary at best.

**Trade-off:** All schemas share one database instance — no physical isolation. Cross-domain queries require joining across schemas, which is disallowed by design (ADR-008). Flyway migration management becomes more complex with multiple schemas.

**Failure mode:** Schema name collisions if naming conventions are not enforced. Developers may accidentally create tables in the wrong schema. Cross-schema queries may be attempted via raw SQL, bypassing the ADR-008 contract. Health checks that ignore the database make the dashboard green while orders fail.

**Revisit when:** A domain outgrows the shared instance and requires its own database server; the team decides to decompose into microservices (each service gets its own DB); cross-domain query performance becomes a measurable bottleneck.
