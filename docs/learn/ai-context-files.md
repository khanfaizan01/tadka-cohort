# AI Context Files

This guide explains how AI tools should read and understand the Tadka codebase.

## How to Approach the Codebase

### 1. Start with the Domain Model

The domain model is the heart of Tadka. Before looking at code, understand the bounded contexts:

- **ordering** — orders, order items, status transitions
- **restaurant** — restaurants, menus, menu items
- **delivery** — deliveries, drivers, tracking
- **identity** — users, roles, authentication
- **payment** — transactions, refunds, payment methods

### 2. Read the ADRs First

Architecture Decision Records in [`docs/adrs/`](./adrs/) capture the "why" behind every structural choice. They are the single source of truth for architectural constraints:

- ADR-001/002/003/008 define the technology stack, monolith-first approach, schema-per-domain, and no-cross-schema-FK rules.
- Any code that appears to violate an ADR should be flagged.

### 3. Understand Schema Boundaries

Each domain lives in its own PostgreSQL schema. JPA entities use `@Table(schema = "...")`. Cross-domain references use UUID identifiers validated in application code — never database foreign keys across schemas.

### 4. Use Flyway Migrations as Documentation

Flyway SQL migration scripts (`src/main/resources/db/migration/`) define the exact database schema. They are the authoritative description of tables, columns, indexes, and constraints.

### 5. Runbooks for Step-by-Step Validation

Daily runbooks in [`docs/runbooks/`](./runbooks/) provide verified commands and expected outputs. Use them to validate any changes against the documented behavior.

### 6. Docker Composition

The infrastructure is defined in `docker-compose.yml` with placeholders for future services. `docker-compose.override.yml` adds development-specific configuration.

## When Generating Code

- Follow existing JPA naming conventions (`@Table(schema = "...")`)
- Place Flyway migrations in the correct versioned directory
- Keep domain logic within its schema's package
- Reference ADRs when proposing structural changes
- Never add a cross-schema foreign key without first updating ADR-008
