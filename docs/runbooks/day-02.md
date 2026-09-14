# Day 2 — Runbook: Domain model & schema-per-domain

**Branch:** `day-02` · **What's new:** five bounded contexts in `domain/`, one Postgres **schema** each, `InitialDomainModel` Flyway migration on startup, **ADR-003** and **ADR-008**. Day 1's liveness `/health` plus the `/health/ready` probe are already in the controller.

> **Windows PowerShell:** use **`curl.exe`**. From the repo root.

| Thing | Value |
|-------|--------|
| API (http) | `http://localhost:8080` |
| Compose service | `postgres` (container `tadka-postgres`) |
| Postgres | `localhost:5432`, db `tadka`, user `tadka`, password `tadka_local` |

Compose cheat sheet: [`docs/learn/docker.md`](../learn/docker.md).

---

## 0. What the tree should look like

```bash
git checkout day-02
mvn compile
```

**Look for**

- Build: **0 errors, 0 warnings**.
- Two modules only: the API and the tests.
- `domain/{orders,restaurants,delivery,identity,payment}` have **classes**, plus `domain/common/` for `Result`, `ResultT`, `DomainException`, `DomainEvent`, `DomainEventDispatcher`, and `domain/valueobjects/` (`Money`, `Address`, `GeoLocation`).
- Folder `identity` vs schema `identity` — intentional (Day 2 teaches that).
- No `k6/`, `terraform/`, extra `src/` services.
- `GET /health` is liveness; `GET /health/ready` hits the DB via `DataSource`.

---

## 1. Fresh Postgres (the `-v` matters)

A volume left over from Day 1 (or a previous Day 2 boot) makes Flyway throw **`duplicate table`** or **`relation already exists`**.

```bash
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

## 2. Run the API (migration on startup)

Keep this terminal open.

```bash
mvn spring-boot:run
```

**Look for**

- `Migrating Flyway migration: V__InitialDomainModel.sql` or `Applying migration 'V__InitialDomainModel'` (first boot on a fresh volume)
- `Tomcat started on port(s): 8080 (http)`

There are still **no** `/api/v1` endpoints. That is Day 3.

---

## 3. Liveness vs readiness

Second terminal, repo root:

```bash
curl http://localhost:8080/health
```

**Look for — HTTP 200**, **no** `database` field:

```json
{ "status": "Healthy", "timestamp": "..." }
```

```bash
curl http://localhost:8080/health/ready
```

**Look for — HTTP 200**

```json
{ "status": "Healthy", "database": "Connected", "responseTimeMs": 3, "timestamp": "..." }
```

First ready hit after `mvn spring-boot:run` is often **500–800 ms**. Hit it again for single-digit ms.

---

## 4. The payoff — five schemas

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\dn"
```

**Look for:** `delivery`, `identity`, `ordering`, `payment`, `restaurant` (plus `public`).

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt ordering.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt restaurant.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt delivery.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt identity.*"
docker exec tadka-postgres psql -U tadka -d tadka -c "\dt payment.*"
```

**Look for (11 tables)**

| Schema | Tables |
|--------|--------|
| `ordering` | `orders`, `order_items`, `coupons`, `coupon_redemptions`, `idempotency_keys` |
| `restaurant` | `restaurants`, `menu_items` |
| `delivery` | `delivery_agents`, `delivery_assignments` |
| `identity` | `users`, `user_addresses` |
| `payment` | `payments` |

---

## 5. ADR-008 and value objects — prove it in the table

No seed data today (that is Day 3). `\d` and the FK query need empty tables.

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\d restaurant.menu_items"
```

**Look for** `price_amount` and `price_currency` as columns on `menu_items` — JPA `@Embeddable` flattened the `Money` value object. There is no `money` table.

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\d ordering.orders"
```

**Look for**

- `customer_id` / `restaurant_id` columns with **no** `FOREIGN KEY` to `identity` or `restaurant`.
- `total_amount_amount`, `total_amount_currency`, `delivery_address_*` — `Money` and `Address` live as **columns** on `orders`, not as separate tables.
- `xmin` column present (ADR-012, added as `@Version @Column(name="xmin")`).

Prove ADR-008 with a query that returns **0 rows** (no seed required):

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "SELECT tc.table_schema, tc.table_name, ccu.table_schema AS foreign_schema, ccu.table_name AS foreign_table FROM information_schema.table_constraints tc JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name AND ccu.constraint_schema = tc.constraint_schema WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_schema <> ccu.table_schema;"
```

Within a schema, FKs **are** allowed:

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\d ordering.order_items"
```

**Look for** a FK from `ordering.order_items` → `ordering.orders`.

---

## 6. (Optional) Postgres down still does not kill liveness

```bash
docker compose stop postgres
curl http://localhost:8080/health/ready
curl http://localhost:8080/health
```

**Look for:** ready **503** / `"Disconnected"`; `/health` still **200**.

```bash
docker compose start postgres
docker compose ps
# wait until healthy, then:
curl http://localhost:8080/health/ready
```

---

## 7. Walk the code (no command)

Open in the editor:

- `domain/orders/Order.java` — aggregate root; `customerId` / `restaurantId` are `UUID`
- `domain/common/Result.java`, `ResultT.java` — domain result wrappers
- `domain/ValueObjects/Money.java` — `@Embeddable`, immutable, `{ amount, currency }`
- `infrastructure/.../TadkaDataSourceConfig.java` or `application.yml` — schema-per-datasource config
- `docs/adrs/003-schema-per-domain.md` and `008-no-cross-schema-fks.md`

---

## 8. Prove ADR-012 columns exist

The `orders` table has an `xmin` column mapped as `@Version` for optimistic concurrency:

```bash
docker exec tadka-postgres psql -U tadka -d tadka -c "\d ordering.orders"
```

**Look for** the `xmin` column (type `bigint`). This is the PostgreSQL system column used for version tracking (ADR-012). The `@Version` annotation tells Hibernate to use it for optimistic locking.

---

## Done when

- [ ] `mvn compile` is clean.
- [ ] `down -v` then `up -d` → `tadka-postgres` healthy.
- [ ] `mvn spring-boot:run` applies `V__InitialDomainModel` with no error.
- [ ] `GET /health` → 200, no `database` field.
- [ ] `GET /health/ready` → 200, `"Connected"`.
- [ ] `\dn` shows the five domain schemas.
- [ ] `ordering.orders` has no FK to `identity` or `restaurant`.
- [ ] You can point at a value object stored as columns, not a table.
- [ ] `xmin` column exists on `ordering.orders`.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `relation already exists` on migrate | `docker compose down -v && docker compose up -d`, then `mvn spring-boot:run` again. |
| `/health/ready` 404 | You are on Day 1 code, or the API was not rebuilt. This branch must have `ready()`. |
| `/health` already returns `database` | Old controller. Day 2 splits liveness and readiness. |
| `psql` role `postgres` does not exist | User is **`tadka`**, database **`tadka`**. |
| Port 8080 in use | Stop the other `mvn spring-boot:run`. |
| `curl` HTML / method error | Use `curl.exe` on PowerShell. |
| Flyway fails with "duplicate table" | Volume was not reset (`-v` missing). `docker compose down -v && docker compose up -d`. |

➡️ Next: Day 3 — the REST API under `/api/v1`.
