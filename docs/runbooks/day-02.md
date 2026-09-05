# Day 2 Runbook: Domain Model + Schema-per-Domain + /health/ready + ADR-003/008

**Goal:** Introduce the domain model, create schemas per domain, add a readiness probe, and validate the no-cross-schema-FK contract.

## Prerequisites

- Completed Day 1 (project compiles, `docker compose up -d` works)
- Java 21, Maven 3.9+, Docker Desktop

## Steps

1. **Compile the project**
   ```bash
   mvn compile
   ```

2. **Recreate infrastructure with volumes reset**
   ```bash
   docker compose down -v && docker compose up -d
   ```

   > The `-v` flag ensures the `pgdata` volume is freshly created, giving you a clean database state.

3. **(Optional) Run the app without Docker**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the liveness endpoint**
   ```bash
   curl http://localhost:5224/health
   ```

5. **Verify the readiness endpoint**
   ```bash
   curl http://localhost:5224/health/ready
   ```

   > **Important:** The first `/health/ready` call is slow (500-800ms) because the application warms up and establishes database connections. **Hit it twice** — the second call should be fast.

6. **Confirm schemas were created**
   ```bash
   docker exec tadka-postgres psql -U tadka -d tadka -c "\dn"
   ```

   Expected output should list: `ordering`, `restaurant`, `delivery`, `identity`, `payment`.

7. **Verify tables inside the `ordering` schema**
   ```bash
   docker exec tadka-postgres psql -U tadka -d tadka -c "\dt ordering.*"
   ```

8. **Prove ADR-008: No cross-schema foreign keys**

   Query the `information_schema` to confirm no foreign keys cross schema boundaries:
   ```sql
   SELECT
       tc.constraint_name,
       tc.table_schema,
       ccu.table_schema AS foreign_table_schema
   FROM information_schema.table_constraints tc
   JOIN information_schema.constraint_column_usage ccu
       ON ccu.constraint_name = tc.constraint_name
   WHERE tc.constraint_type = 'FOREIGN KEY'
       AND tc.table_schema <> ccu.table_schema;
   ```

   Expected result: **zero rows**. Every FK must be within a single schema.

## Notes

- **`/health/ready`** is the Spring Boot readiness indicator. The first invocation triggers connection pool initialization and schema verification, causing the 500-800ms delay.
- **Schema-per-domain** (ADR-003) is enforced by Flyway migration scripts and JPA `@Table(schema = "...")` annotations.
- **No cross-schema FKs** (ADR-008) is validated by the query above. Cross-domain references use UUID identifiers validated in application code.
- The database schemas are created automatically by Flyway on application startup.
