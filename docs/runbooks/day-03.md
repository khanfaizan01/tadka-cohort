# Day 3 — Runbook: REST API, server-side pricing, RFC 7807

**Branch:** `day-03` · **What's new:** `/api/v1` restaurants + orders, `OrderFactory` prices from the menu, order state machine, RFC 7807, seed (3 restaurants / 16 items / Priya). Day 2 schemas and Day 1 `/health` + `/health/ready` stay.

> **Windows PowerShell:** use **`curl.exe`**. From the repo root.

| Thing | Value |
|-------|--------|
| API (http) | `http://localhost:5224` |
| Compose service | `postgres` (container `tadka-postgres`) |
| Postgres | `localhost:5432`, db `tadka`, user `tadka`, password `tadka_local` |

### pgAdmin (or any GUI) from your machine

Host `localhost` (or `127.0.0.1`), port `5432`, database `tadka`, user `tadka`, password `tadka_local`. **Do not** use Host `tadka-postgres`. After `mvn spring-boot:run`, schemas `ordering` / `restaurant` / … have seed rows.

### Docker — commands in this file

| Command | What it does |
|---------|----------------|
| `docker compose down -v` | Stop and **wipe** the volume. Required when switching onto `day-03` so seed migrations apply. |
| `docker compose up -d` | Start Postgres in the background. |
| `docker compose ps` | Want `tadka-postgres` **`(healthy)`**. |
| `docker compose stop postgres` | Pause the DB. Data stays. Liveness vs ready demo. |
| `docker compose start postgres` | Resume. Wait for `(healthy)`. |

Service name is **`postgres`**, container **`tadka-postgres`**. `docker compose stop db` fails.

**Container name already in use:** another clone already started `tadka-postgres`. `docker compose down` there, then `up -d` here.

Stable seed GUIDs (clipboard / sticky note):

| | |
|--|--|
| Meghana Foods | `a1b2c3d4-0001-4000-8000-000000000001` |
| Chicken Biryani (₹299) | `b1b2c3d4-0001-4000-8000-000000000001` |
| Priya Sharma | `c1b2c3d4-0001-4000-8000-000000000001` |

---

## 0. Fresh volume

A Day-2 volume is missing the seed migration. Always:

```bash
git checkout day-03
mvn compile
docker compose down -v
docker compose up -d
docker compose ps
```

**Look for:** `tadka-postgres` **`(healthy)`**. Build: **0 errors**.

---

## 1. Run the API

```bash
mvn spring-boot:run
```

**Look for:** applying `InitialDomainModel` then `OrderLifecycleAndDemoSeed`; listening on **5224**.

---

## 2. Health is still split

```bash
curl -s http://localhost:5224/health
# { "status": "Healthy", "timestamp": "..." }   — no database field

curl -s http://localhost:5224/health/ready
# { "status": "Healthy", "database": "Connected", ... }
```

---

## 3. Browse (seed)

```bash
curl -s http://localhost:5224/api/v1/restaurants
curl -s http://localhost:5224/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001/menu
```

**Look for:** Meghana Foods, Truffles, Vidyarthi Bhavan. Menu has 16 items across the three. Chicken Biryani **299**.

---

## 4. Place an order — no price in the request

```bash
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:5224/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"c1b2c3d4-0001-4000-8000-000000000001","restaurantId":"a1b2c3d4-0001-4000-8000-000000000001","items":[{"menuItemId":"b1b2c3d4-0001-4000-8000-000000000001","quantity":2}],"deliveryAddress":{"line1":"302, Prestige Lakeside","line2":"Whitefield","city":"Bangalore","pincode":"560066","latitude":12.97,"longitude":77.75}}'
```

**Look for — HTTP 201.** `totalAmount.amount` is **598.00**. Request had no price. Save the order `id`.

---

## 5. State machine and error shape

```bash
# legal → 204
curl -s -w "\nHTTP %{http_code}\n" -X PATCH http://localhost:5224/api/v1/orders/<ORDER_ID>/status \
  -H "Content-Type: application/json" -d '{"status":"Confirmed"}'

# illegal jump on a NEW order → 422
curl -s -w "\nHTTP %{http_code}\n" -X PATCH http://localhost:5224/api/v1/orders/<NEW_ORDER_ID>/status \
  -H "Content-Type: application/json" -d '{"status":"Delivered"}'

# unknown id → 404
curl -s -w "\nHTTP %{http_code}\n" http://localhost:5224/api/v1/orders/00000000-0000-0000-0000-000000000000

# empty items → 400 with an errors map
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:5224/api/v1/orders \
  -H "Content-Type: application/json" -d '{"items":[]}'
```

**Look for:** 204 / 422 / 404 / 400. Bodies are RFC 7807 (`type`, `title`, `status`, `detail`). 400 has `errors`.

---

## 6. Partner edit — PATCH, not PUT

```bash
curl -s -w "\nHTTP %{http_code}\n" -X PATCH http://localhost:5224/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001/menu/b1b2c3d4-0001-4000-8000-000000000001 \
  -H "Content-Type: application/json" -d '{"price":{"amount":320}}'

curl -s http://localhost:5224/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001/menu

curl -s -w "\nHTTP %{http_code}\n" -X PATCH http://localhost:5224/api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001 \
  -H "Content-Type: application/json" -d '{"isActive":false}'
```

**Look for:** 204, then Chicken Biryani **320**. Restaurant deactivate is PATCH `isActive`, not DELETE.

## 7. Idempotency — Beat 1 (optional demo)

Two POSTs, **no** key → **two different** order ids.

```bash
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:5224/api/v1/orders -H "Content-Type: application/json" --data-binary @docs/runbooks/place-order.json
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:5224/api/v1/orders -H "Content-Type: application/json" --data-binary @docs/runbooks/place-order.json
```

Same `Idempotency-Key` twice → first **201**, second **200**, **same** id.

```bash
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:5224/api/v1/orders -H "Content-Type: application/json" -H "Idempotency-Key: demo-key-123" --data-binary @docs/runbooks/place-order.json
curl -s -w "\nHTTP %{http_code}\n" -X POST http://localhost:5224/api/v1/orders -H "Content-Type: application/json" -H "Idempotency-Key: demo-key-123" --data-binary @docs/runbooks/place-order.json
```

Do **not** demo same key + different body. The API still returns the first order (ADR-011 revisit: Stripe would 422).

## 8. (Optional) liveness still ignores Postgres

```bash
docker compose stop postgres
curl -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health
curl -s -w "\nHTTP %{http_code}\n" http://localhost:5224/health/ready
docker compose start postgres
```

**Look for:** `/health` **200**; `/health/ready` **503**.

---

## Done when

- [ ] `mvn compile` is clean.
- [ ] `down -v` then `up -d` → postgres healthy.
- [ ] `mvn spring-boot:run` applies both migrations; restaurants = 3.
- [ ] POST order with no price → 201 / **598.00**.
- [ ] Illegal status → 422; missing id → 404; empty items → 400; same problem+json shape.
- [ ] PATCH price then GET menu shows the new amount.
- [ ] `/health` has no `database` field.
- [ ] Idempotency key demo works (201 then 200).

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `relation already exists` | Old Day 2/3 volume. Ctrl+C the API, then the wipe block in **§0** (`down -v` **and** `docker rm -f tadka-postgres` + both `pgdata` volumes). Then `up -d` and `mvn spring-boot:run`. |
| Empty restaurants | You are on Day 2 volume or Day 2 code. This branch must apply `OrderLifecycleAndDemoSeed`. |
| `/health` returns `database` | Old controller. Day 3 keeps the Day 1/2 split. |
| `curl` HTML / method error | Use `curl.exe` on PowerShell, or `curl` on Git Bash. |
| POST 400 on a good body | JSON quoting in PowerShell. Use a file (`--data-binary @file.json`). |
| Two POSTs with a key still create two orders | Header name is `Idempotency-Key`. Same key both times. |

➡️ Next: Day 4 — order-flow hardening (idempotency, `xmin` → 409, domain events).
