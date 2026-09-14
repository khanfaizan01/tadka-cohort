# ADR-004: Spring Data JPA as ORM

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Object-relational mapping in Java. EF Core in .NET maps cleanly to Spring Data JPA + Hibernate.

**Options:**
1. Spring Data JPA with Hibernate (the Spring-native choice).
2. MyBatis / MyBatis-Plus (SQL mapper).
3. JPA with raw EntityManager only (no repository abstraction).
4. jOOQ (type-safe SQL builder).

**Choice:** Option 1. Spring Data JPA with Hibernate. `@Entity`, `@Table(schema = "...")`, `@Embeddable`, `@OneToMany`, `@ManyToOne` mirror EF Core annotations. Spring Data JPA repositories (`JpaRepository`) replace `DbSet<T>`.

**Why:** Spring Boot ships with it. The `@Entity` → `@Table(schema=...)` mapping is a near-verbatim port from EF Core's `ToTable("orders", "ordering")`. Spring Data JPA's derived query methods replace most manual SQL. Flyway handles the DDL independently.

**Trade-off:** Hibernate's lazy loading can surprise developers (`LazyInitializationException`). N+1 queries require `@EntityGraph` or `JOIN FETCH`. PostgreSQL `xmin` for optimistic locking works but Hibernate's `@Version` needs the column mapped explicitly.

**Failure mode:** Using `@ManyToOne` without `@JsonIgnore` creates infinite JSON cycles (restaurant → menu → restaurant). The fix is `@JsonIgnore` on the child side, not removing the relationship.

**Revisit when:** The read path becomes the bottleneck (complex reporting queries). Then a CQRS read model with a separate query service is the seam, not replacing JPA wholesale.
