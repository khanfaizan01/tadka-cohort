# Tadka Learning Path

A curated set of documents to guide the cohort through building the Tadka food delivery platform.

## Learning Tracks

- **[Docker](./docker.md)** — Compose cheat sheet and infrastructure basics
- **[AI Context Files](./ai-context-files.md)** — How AI tools should read this codebase

## Daily Runbooks

| Day | Focus |
|-----|-------|
| Day 1 | Scaffold + liveness health endpoint + ADR-001/002 |
| Day 2 | Domain model + schema-per-domain + /health/ready + ADR-003/008 |

## Architecture Decision Records (ADRs)

All architectural decisions are documented in [`docs/adrs/`](./adrs/). Each ADR follows a standard template (`docs/templates/adr-template.md`).

- ADR-001: Java 21 + Spring Boot 3.5.16
- ADR-002: Monolith-first
- ADR-003: Schema-per-domain
- ADR-008: No cross-schema foreign keys

## Diagrams

Architecture and sequence diagrams are stored in [`docs/diagrams/`](./diagrams/).
