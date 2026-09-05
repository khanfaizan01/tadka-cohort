# ADR-002: Monolith-first

**Date:** 2026-08-28

**Status:** Accepted

**Deciders:** Architecture team / Tadka cohort

## Context

The Tadka food delivery platform is being built from scratch. The team is small, and the domain is cohesive (ordering, restaurants, deliveries, identity, payments). Deciding on an initial architectural style is critical to avoid premature decomposition while not boxing the team into a dead end.

## Decision

Start with a **monolith-first** architecture: one project, one database, one `docker-compose.yml` file.

All bounded contexts (ordering, restaurant, delivery, identity, payment) reside within a single deployable application, sharing one PostgreSQL 16 instance with schema-per-domain separation (see ADR-003).

## Consequences

### Positive

- Simplest possible operational footprint for a small team
- No inter-service network calls to debug or secure early on
- Transactional boundaries are straightforward within a single database
- Easy to reason about the full codebase in one place
- Deployment is a single artefact (`docker compose up`)

### Negative

- A single failure can bring down all domains
- Scaling is coarse-grained (must scale the whole app)
- Technology homogenization — every domain must use the same stack
- As the codebase grows, build and startup times increase

### Risks

- Teams may become comfortable with the monolith and delay decomposition indefinitely
- Shared database can become a coupling vector if schema boundaries are not enforced

## Alternatives Considered

### Option A: Microservices from day one

- **Pros:** Independent deployability, fine-grained scaling, isolation of failures
- **Cons:** Massive operational overhead for a small team, distributed-system complexity, network latency, data consistency challenges
- **Why rejected:** Premature decomposition introduces complexity that outweighs the benefits for a team of fewer than 8 engineers and a single-domain application.

### Option B: Modular monolith with separate deployables

- **Pros:** Some isolation while keeping a single deployment
- **Cons:** Adds packaging complexity without the operational benefits of true microservices
- **Why rejected:** Adds complexity of multiple deployables without solving the core team-size problem; the monolith-first approach is simpler and sufficient.

## References

- ADR-001: Use Java 21 with Spring Boot 3.5.16
- ADR-003: Schema-per-domain in single Postgres DB

## Revisit When

- Team size exceeds **8 engineers**
- A **measurable per-service bottleneck** is identified (e.g., ordering service consistently hits resource limits while delivery service is idle)
- An **independent deploy cadence** is required by a specific domain (e.g., payment updates must ship independently of restaurant updates)
- A **third-party SLA** forces isolation (e.g., a payment provider mandates network-level segregation)
