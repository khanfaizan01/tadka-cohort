# Day 1 — Runbook: Scaffold + `/health`

**Branch:** `day-01` · **What exists today:** one Spring Boot 3.5.16 API, one PostgreSQL 16 database, a liveness `/health` that does **not** talk to Postgres. Nothing else — and that is the point.

Compose commands and service vs container name: [`docs/learn/docker.md`](../learn/docker.md).

| Thing | Value |
|-------|--------|
| API (http) | `http://localhost:5224` |
| Compose service | `postgres` (container `tadka-postgres`) |
| Postgres | `localhost:5432`, db `tadka`, user `tadka`, password `tadka_local` |

> **Windows PowerShell:** use **`curl.exe`**. From the repo root. Quote `@file` so PowerShell does not splat the path.

### Local dummy password — not a production secret

`application.properties` contains the dummy password for the local Docker database with no real data.

Spring Boot config is layered (later wins): `application.properties` (base) → `application-dev.yml` (local dummy, in git, throwaway DB). When the password is real it never lives in git — env / secret store / Vault in deploy (Day 10). Do not copy the dev file into production config.

---

## 0. Fresh start (pre-class)

```bash
git checkout day-01
docker compose down -v
docker compose up -d
docker compose ps
```

**Look for:** `tadka-postgres` … **`(healthy)`**.

```bash
docker exec tadka-postgres pg_isready -U tadka
# → localhost:5432 - accepting connections
```

---

## 1. Build and run the API

Keep this terminal open.

```bash
mvn clean package -DskipTests -q
java -jar target\tadka-api-0.0.1-SNAPSHOT.jar
```

**Look for**

```
Tomcat started on port(s): 5224 (http)
Started Application in ... seconds
```

There is **no** migration on Day 1. Flyway is configured but has no SQL files yet. If you see Flyway applying migrations, you are on the wrong branch.

Optional: Swagger UI at `http://localhost:5224/swagger-ui.html`.

---

## 2. Baseline — liveness only

In a **second** terminal, from the repo root:

```bash
curl http://localhost:5224/health
```

**Look for — HTTP 200**

```json
{"status":"UP"}
```

What must **not** be there: `database`, `responseTime`. This endpoint only means "the process is up." A load balancer that trusts this while Postgres is down is being lied to. That is the setup for the next step.

---

## 3. Live build — add `/health/ready`

This is the Day 1 demo. Open the repo in VS Code, and use Copilot / your IDE to add the readiness probe. The prompt:

```
Add a /health/ready endpoint to HealthController that checks PostgreSQL connectivity.
It should attempt a simple query using JdbcTemplate or a repository, measure the response time, and return:
- database status (healthy/unhealthy)
- response time in milliseconds
- overall status based on all checks

Return 200 if healthy, 503 if any check fails.
```

**Look for in the generated code**

- It **injects** the existing `DataSource` or `JdbcTemplate` — it does not open a new connection.
- `@Async`/`CompletableFuture` or synchronous — but **not** `.get()` blocking in the controller thread.
- A **new** action (`GET /health/ready`), not a rewrite of `GET /health`.
- Controller style (not `@RestController` functional endpoints) — match the existing codebase style.

If you prefer, add this by hand in `HealthController` (constructor + `ready()` method), then continue. The break/fix below is the real lesson; IDE assistance is the generator.

Restart the API (`Ctrl+C` in the run terminal, then `java -jar target\tadka-api-0.0.1-SNAPSHOT.jar` again) so the new action is loaded.

---

## 4. Ready check — Postgres up

```bash
curl http://localhost:5224/health/ready
```

**Look for — HTTP 200**

Something in this shape (field names may vary slightly):

```json
{
  "status": "healthy",
  "database": "Connected",
  "responseTimeMs": 3,
  "timestamp": "..."
}
```

**Cold start:** the **first** `/health/ready` after `java -jar` is often **500–800 ms** (Spring context refresh + HikariCP pool + schema verification). Hit it again. Steady state should be **single-digit ms**. That is why we measure p99 on a warm system, not the first request.

Leave `/health` alone and hit it too — it must still be the simple liveness payload from step 2.

---

## 5. Break — stop Postgres

Compose service name is **`postgres`**, not `db`.

```bash
docker compose stop postgres
curl http://localhost:5224/health
curl http://localhost:5224/health/ready
```

**Look for**

| Endpoint | HTTP | Body |
|----------|------|------|
| `/health/ready` | **503** | `"unhealthy"` / `"Disconnected"` plus a response time |
| `/health` | **200** | `{"status":"UP"}` — **no database field** |

That split is the whole demo: process-up is not the same as system-healthy.

---

## 6. Fix — start Postgres

```bash
docker compose start postgres
docker compose ps
# wait until tadka-postgres is (healthy), then:
curl http://localhost:5224/health/ready
```

**Look for** — `/health/ready` is **200** again, database connected, single-digit ms after the first hit.

---

## 7. Tests (optional)

```bash
mvn test
```

**Look for** — 1 passing placeholder test. Day 1 does not have health integration tests yet.

---

## Done when

- [ ] `mvn compile` is clean (0 errors).
- [ ] `docker compose ps` shows `tadka-postgres` **healthy**.
- [ ] `GET /health` → **200**, `{"status":"UP"}`, **no** `database` field.
- [ ] `GET /health/ready` added and uses the existing `DataSource`/`JdbcTemplate`.
- [ ] First `/health/ready` may be hundreds of ms; the next one is single-digit ms.
- [ ] `docker compose stop postgres` → `/health/ready` **503**, `/health` still **200**.
- [ ] `docker compose start postgres` → `/health/ready` **200** again.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `tadka-postgres` not healthy | `docker compose logs postgres`. Wait 10 s. If port 5432 is taken, stop the other Postgres. |
| `Failed to bind ... 5224` | A previous `java -jar` is still up. Stop it, or `Get-NetTCPConnection -LocalPort 5224` and kill that PID. |
| `curl` prints a huge HTML / method error | You used PowerShell `curl`. Switch to `curl.exe`. |
| `docker compose stop db` fails | Service is `postgres`. |
| `/health` already returns `database` / `Connected` | You are not on the Day 1 scaffold (or you already applied the ready check to `/health`). `GET /health` must stay liveness-only. |
| `/health/ready` 404 | API was not restarted after editing the controller. |
| Ready stays 503 after `start postgres` | Container is up but not healthy yet. `docker compose ps` until `(healthy)`, then retry. |
| Reset the database volume | `docker compose down -v` then `docker compose up -d`. Day 1 has no seed data to lose. |

Stop the API with `Ctrl+C`. Leave Postgres running for the rest of the session, or `docker compose stop postgres` when you are done.
