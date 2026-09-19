# Day 5 — Runbook: Read replica, connection pooling, indexes

**Branch:** `day-05`. **What's new:** PostgreSQL streaming read replica (ADR-016), connection pooling, indexing strategy (ADR-014), performance indexes via Flyway (`V6__AddPerformanceIndexes.sql`). Day 4's API stays.

> **Windows PowerShell:** use **`curl.exe`**. From the repo root. Quote `@file` so PowerShell does not splat the path.

### Demo → code

| When (script) | What you run | What it proves | Code to point at |
|---|---|---|---|
| Beat 1 — primary vs replica | `GET /orders?customerId=...` on primary, then replica | Reads from replica, writes go to primary | `TadkaReadDbContext`, `TadkaDbContext` |
| Beat 2 — connection pool | `pgbouncer` exhaustion demo | Pool sizing matters | `docs/database/connection-pooling-guide.md` |
| Beat 3 — index lookup | `EXPLAIN` on customer order query | Index makes it fast | `V6__AddPerformanceIndexes.sql` |

| Thing | Value |
|-------|--------|
| API | `http://localhost:5224` |
| Primary | `localhost:5432` |
| Replica | `localhost:5433` (scale-out profile) |
| Postgres | db/user `tadka`, password `tadka_local` |

**Do not open for the spine:** `CouponsController` (leftover 50-way), `Demo:DispatchEventsBeforeCommit` (Day 7), `cursor-pagination-toy` (Day 5).

---

## 0. Fresh volume + tests (pre-class)

Day 2/3/4 already created `ordering.orders`. If that volume is still there, `mvn spring-boot:run` tries to `CREATE TABLE` again → **`42P07: relation "orders" already exists`**. `down` without **`-v`** is not enough. The container name is always **`tadka-postgres`**, so **another clone** can keep the old volume alive.

```powershell
git checkout day-05

# wipe THIS project's compose volume
docker compose down -v
docker rm -f tadka-postgres
docker volume rm tadka_pgdata tadka-cohort_pgdata

docker compose up -d
docker compose ps              # tadka-postgres (healthy)

mvn compile
mvn test
mvn spring-boot:run
```

If `volume rm` says "no such volume", ignore it. If it says "volume is in use", `docker rm -f tadka-postgres` again, then `volume rm`.

**Look for:** tests pass. Migrations `InitialDomainModel`, `OrderLifecycleAndDemoSeed`, `Day04Hardening`, `V6__AddPerformanceIndexes` applied. Listen **5224**. Docker Desktop must be running (Testcontainers).

---

## 1. Run it (primary + replica)

```powershell
# Start primary + replica (scale-out profile enables the postgres-replica service)
docker compose --profile scale-out up -d
docker compose ps
```

**Look for:** two containers — `tadka-postgres` and `tadka-postgres-replica`, both **`(healthy)`**. The replica logs show **`started streaming WAL from primary`**.

```bash
mvn spring-boot:run
```

The app migrates the **primary** on startup. The replica streams via logical replication (WAL).

Verify the replica is live and **read-only**:

```powershell
docker exec tadka-postgres psql -U tadka -d tadka -c "SELECT count(*) FROM restaurant.restaurants;"   # 3 (streamed from primary)
docker exec tadka-postgres psql -U tadka -d tadka -c "INSERT INTO restaurant.restaurants(id,name) VALUES (gen_random_uuid(),'x');"   # ERROR: read-only standby
curl -s http://localhost:5224/api/v1/restaurants    # served FROM the replica (read context)
```

---

## 2. Indexing — see the Seq Scan, then fix it (ADR-014)

The Flyway migration `V6__AddPerformanceIndexes.sql` (`src/main/resources/db/migration/`) creates all indexes. They're applied automatically on startup. To see the **before** state, drop them and `EXPLAIN`.

**Before** — drop the indexes and see the sequential scan on ~200k rows:

```powershell
# Drop the performance indexes to induce the break
docker exec -i tadka-postgres psql -U tadka -d tadka <<'SQL'
DROP INDEX IF EXISTS idx_orders_customer_created;
DROP INDEX IF EXISTS idx_orders_status;
DROP INDEX IF EXISTS idx_idempotency_keys_key;
DROP INDEX IF EXISTS idx_coupons_code;
DROP INDEX IF EXISTS idx_coupon_redemptions_coupon;
SQL

# Bloated table: run the order-history query WITHOUT indexes
docker exec tadka-postgres psql -U tadka -d tadka -c "EXPLAIN ANALYZE SELECT * FROM ordering.orders WHERE customer_id='00000000-0000-0000-0000-000000000000' ORDER BY created_at DESC LIMIT 10;"
# → Seq Scan on orders ... Execution Time ~100+ ms
```

**After** — recreate the indexes via Flyway and `EXPLAIN` again:

```powershell
# Recreate indexes by re-applying V6 (or restart the app on a fresh volume)
docker compose down -v && docker compose up -d
mvn spring-boot:run

docker exec tadka-postgres psql -U tadka -d tadka -c "EXPLAIN ANALYZE SELECT * FROM ordering.orders WHERE customer_id='00000000-0000-0000-0000-000000000000' ORDER BY created_at DESC LIMIT 10;"
# → Index Scan using idx_orders_customer_created ... Execution Time ~3-5 ms
```

**What `V6__AddPerformanceIndexes.sql` creates:**

| Index | Column | Purpose |
|---|---|---|
| `idx_orders_customer_created` | `(customer_id, created_at DESC)` | Customer order history (GET `/orders?customerId=`) |
| `idx_orders_status` | `(status)` | Live tracking status lookup |
| `idx_idempotency_keys_key` | `("key")` | Idempotency key lookup |
| `idx_coupons_code` | `(code)` | Coupon validation |
| `idx_coupon_redemptions_coupon` | `(coupon_id, redeemed_at DESC)` | Coupon redemption tracking |

> **Note:** `idx_idempotency_keys_key` quotes `"key"` because `key` is a PostgreSQL reserved word. `idx_coupon_redemptions_coupon` uses `coupon_id` + `redeemed_at` (not `created_at` as the coupon table uses).

**Trade-off:** every index taxes writes — we add only the query-justified ones, and deliberately do **not** over-index.

---

## 3. Read replica + read-your-writes / CAP (ADR-016)

A read of an order you *just* placed must hit the **primary** (the replica is milliseconds behind). The app routes it correctly:

```powershell
ORDER=$(curl -s -X POST http://localhost:5224/api/v1/orders -H "Content-Type: application/json" -d '{"customerId":"c1b2c3d4-0001-4000-8000-000000000001","restaurantId":"a1b2c3d4-0001-4000-8000-000000000001","items":[{"menuItemId":"b1b2c3d4-0001-4000-8000-000000000001","quantity":1}],"deliveryAddress":{"line1":"x","line2":"y","city":"Bangalore","pincode":"560066","latitude":12.9,"longitude":77.7}}')

# The just-placed-order read comes from the PRIMARY (always visible)
curl -s -o /dev/null -w "GET just-placed order: %{http_code}\n" http://localhost:5224/api/v1/orders/$ORDER   # 200
```

> List/menu/order-history reads come from the replica (stale-tolerant); the just-placed-order read comes from the primary (read-your-writes).

---

## 4. Measure under load (before/after)

**Canonical k6 dinner-rush** (install once: `winget install GrafanaLabs.k6`):

```powershell
k6 run k6/dinner-rush.js
# → 10→50→0 stages, checks 100%
```

The `k6/dinner-rush.js` script targets `GET /api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001` with stages ramping from 10 → 50 → 0 VUs. This exercises both the index (if the restaurant menu is cached) and the rate limiter (Beat 7 on day-06).

---

## 5. Connection pool exhaustion (ADR-015)

**Reproduce the "everything goes slow" break** by shrinking the pool while the index is dropped, then load it:

```powershell
# Drop indexes, start the app with a tiny pool
docker exec -i tadka-postgres psql -U tadka -d tadka <<'SQL'
DROP INDEX IF EXISTS idx_orders_customer_created;
DROP INDEX IF EXISTS idx_orders_status;
SQL

# Override the datasource to a tiny pool (restart the app)
# In application.properties or via env: spring.datasource.hikari.maximum-pool-size=2
java -jar target\tadka-api-0.0.1-SNAPSHOT.jar
```

In another terminal, hammer it:

```powershell
k6 run k6/dinner-rush.js
# → p99 collapses (hundreds–1000+ ms under contention). Restore: stop app, run V6, restart with default pool.
```

> Honest note: on a single warm laptop you can't fully *exhaust* 2 connections — exhaustion is `concurrency × hold-time × pool-size`, a **multi-instance** failure. The contention p99 spike is still the visible symptom. PgBouncer is earned at Day 11.

---

## ✅ Done when

- [ ] Both Postgres containers healthy (`docker compose ps`); replica returns seeded rows and **rejects writes**.
- [ ] `EXPLAIN` shows **Seq Scan (~100+ ms)** before the index and **Index Scan (~3-5 ms)** after, on ~200k rows.
- [ ] A just-placed order is visible via `GET /orders/{id}` (primary); the replica shows a real lag.
- [ ] `V6__AddPerformanceIndexes.sql` applied cleanly with no error.
- [ ] `k6/dinner-rush.js` runs without errors (rate limiter + connection pool both stable).
- [ ] You can name each index and the query it serves.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| Replica won't start / not healthy | It clones the primary on first boot. Reset cleanly: `docker compose down -v && docker compose up -d`. |
| `EXPLAIN` shows Seq Scan after restart | The index was dropped during the demo. Re-apply `V6__AddPerformanceIndexes.sql` or restart on a fresh volume. |
| `42P07: relation "orders" already exists` | Old volume. `docker compose down -v && docker compose up -d`, then `mvn spring-boot:run`. |
| `k6: command not found` | Install: `winget install GrafanaLabs.k6`. |
| `curl` HTML / method error | Use `curl.exe` on PowerShell. |
| `mvn test` hangs / Docker errors | Start Docker Desktop; Testcontainers needs it. |
| Port 5224 in use | Stop the other `java -jar`. |
| `V6` migration fails | Check `idx_idempotency_keys_key` — `"key"` is quoted because `key` is a PostgreSQL reserved word. |
| Pool exhaustion not reproduced | Need concurrent connections > pool size. Use `k6` with enough VUs, or shrink `maximum-pool-size` to 2. |

➡️ Next: [day-06.md](day-06.md) — Redis cache + live order tracking.
