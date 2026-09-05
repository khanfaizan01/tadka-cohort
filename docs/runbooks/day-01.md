# Day 1 Runbook: Scaffold + Liveness + ADR-001/002

**Goal:** Get the project compiling and a health endpoint responding. Establish the architectural decisions.

## Prerequisites

- Java 21 installed (`java -version`)
- Maven 3.9+ installed (`mvn -version`)
- Docker Desktop running

## Steps

1. **Compile the project**
   ```bash
   mvn compile
   ```

2. **Start infrastructure**
   ```bash
   docker compose up -d
   ```

3. **(Optional) Run the app without Docker**
   ```bash
   mvn spring-boot:run
   ```

4. **Verify the health endpoint**
   ```bash
   curl http://localhost:5224/health
   ```

   Expected response: a successful health check from the liveness probe.

## Notes

- **Two projects only** — no business endpoints yet. The scaffold provides the application shell and the `/health` liveness endpoint.
- **Port 5224** is the application's server port.
- **No database interaction** on Day 1. The `/health` endpoint does not depend on PostgreSQL connectivity.
- **ADR-001** (Java 21 + Spring Boot 3.5.16) and **ADR-002** (monolith-first) are documented in `docs/adrs/`.
- The Day 2 runbook extends this with `/health/ready` and domain schema setup.
