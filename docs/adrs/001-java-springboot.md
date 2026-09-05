# ADR-001: Use Java 21 with Spring Boot 3.5.16

**Date:** 2026-08-28

**Status:** Accepted

**Deciders:** Architecture team / Tadka cohort

## Context

The original Tadka codebase was implemented in .NET 10. The cohort has decided to re-implement the platform in Java to deepen JVM ecosystem expertise. The platform is a food delivery application requiring a robust backend framework, mature ORM, and strong typing.

## Decision

Re-implement Tadka in **Java 21** with **Spring Boot 3.5.16**, using:

- **Spring Data JPA** for data access and repository patterns
- **Flyway** for database migrations
- **PostgreSQL 16** as the database engine

## Consequences

### Positive

- Mature ecosystem with extensive community support and documentation
- Team familiarity with the Java/Spring stack
- Strong typing reduces runtime errors
- Spring Boot 3.x provides modern, native-image-ready architecture
- Spring Data JPA dramatically reduces boilerplate data access code
- Flyway offers version-controlled, testable migrations

### Negative

- Increased verbosity compared to C# / .NET
- Slower compile-to-run feedback loop versus interpreted languages
- JVM memory footprint is larger than alternatives like Go or Node.js

### Risks

- Tighter coupling to the JVM ecosystem; switching frameworks later is costly
- Spring Boot version lock-in may delay upgrading to future Java LTS releases
- Flyway migration scripts must be kept in sync across environments

## Alternatives Considered

### Option A: Continue with .NET 10

- **Pros:** Existing codebase, fastest path to MVP, strong type system, C# language ergonomics
- **Cons:** Cohort goal is to learn Java; would not fulfill learning objectives
- **Why rejected:** The cohort explicitly decided to invest in Java skills rather than continue on a familiar stack.

### Option B: Use Go

- **Pros:** Excellent concurrency model, small binaries, fast runtime, strong for microservices
- **Cons:** Weak domain-modeling capabilities, fewer ORM options, less mature ecosystem for complex business logic
- **Why rejected:** Poor support for rich domain modeling; the food delivery domain (orders, restaurants, deliveries, payments) requires the expressive object-relational mapping that Spring Data JPA provides.

### Option C: Use Node.js

- **Pros:** Fast development cycle, large npm ecosystem, full-stack JavaScript
- **Cons:** Weaker type system (even with TypeScript), less mature ORM landscape, runtime flexibility introduces subtle bugs
- **Why rejected:** The ORM maturity and type safety of Spring Data JPA are superior for a domain-heavy application like food delivery.

## References

- ADR-002: Monolith-first architecture

## Revisit When

- Team outgrows Spring Boot's opinionated structure
- Performance profiling indicates JVM is a bottleneck
- A major Spring Boot version upgrade breaks backward compatibility
