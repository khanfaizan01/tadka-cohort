# Day 6 — Runbook: Redis cache-aside, stampede lock, SSE live tracking, rate limiting, signed URLs

**Branch:** `day-06`. **What's new:** Redis cache-aside + stampede lock (ADR-018/019), live tracking SSE + Redis pub/sub backplane (ADR-020), reconnect replay buffer (ADR-051), distributed rate limiting (ADR-049), conditional GET ETag (ADR-048), signed invoice URLs (ADR-050), stateless scale-out preview (ADR-047).

**Two containers:** Postgres primary `5432`, **Redis `6379`**. HTTP **5224**. Tests **28/28**.

> **Windows PowerShell:** use **`curl.exe`**. Quote `@file` so PowerShell does not splat the path. `$RID`, `$ITEM`, `$ORDER`, and `$KEY` are PowerShell variables you set once — leave them **unquoted** so they expand. Redis CLI walkthrough (PING, SET NX, pub/sub): [`docs/database/redis-cli.md`](../database/redis-cli.md).

| Thing | Value |
|-------|--------|
| API | `http://localhost:5224` |
| Redis | `localhost:6379` · container `tadka-redis` |
| Postgres | `localhost:5432`, db/user `tadka` / `tadka_local` |
| Meghana | `a1b2c3d4-0001-4000-8000-000000000001` |
| Biryani | `b1b2c3d4-0001-4000-8000-000000000001` |
| Cache key | `restaurant:{Meghana}:menu` |
| Menu TTL | 60 s (`RestaurantsController` calls `ICacheService` with 60 s TTL) |
| Rate limit | 120 req/min fixed window, 60 s window (`RedisFixedWindowRateLimiter`) |
| Signed URL validity | 5 min (`OrderInvoiceController.DEFAULT_VALIDITY`) |
| SSE replay buffer | last 20 events, 6 h TTL (`RedisOrderTrackingBus`) |

Spoken cue in class: **"Ab demo."**

### Demo → code

| When (script) | What you run | What it proves | Code to point at |
|---|---|---|---|
| Beat 1 — cache-aside | `DEL` key, GET menu twice with `time_total` | Miss→hit, TTL ~60, second GET faster | `RedisCacheService.getOrSet` |
| Beat 2 — stampede | 100 concurrent GET `/restaurants/{id}` | Exactly one refresher, others wait | `RedisCacheService` SET NX lock |
| Beat 3 — Redis down | `docker compose stop redis`, menu + SSE | menu **200**, SSE **503** | `RedisCacheService` catch `DataAccessException`; `OrderTrackingController` `isEnabled()` |
| Beat 4 — delete-on-write | PATCH availability, then GET | `EXISTS` 0, next GET → 1 | `RestaurantsController.updateMenuItemAvailability` → `invalidate` |
| Beat 5 — SSE live tracking | `curl.exe -N` + PATCH Confirmed | `event: Confirmed` pushed | `OrderTrackingController`, `RedisOrderTrackingBus.publishAsync` |
| Beat 6 — reconnect replay | GET `/events` with `Last-Event-ID` | Replays missed events | `RedisOrderTrackingBus.getEventsSince` |
| Beat 7 — rate limiting | `k6 run k6/dinner-rush.js` | 429 after limit, `Retry-After` | `RateLimitingMiddleware`, `RedisFixedWindowRateLimiter` |
| Beat 8 — conditional GET | `GET` then `GET` with `If-None-Match` | 304 on second request | `ETagFilterAttribute` |
| Beat 9 — signed URL | POST `/invoice/sign`, GET with `?sig=&exp=` | Time-limited, tamper-evident | `UrlSigner`, `OrderInvoiceController` |

### Tools

| Tool | Purpose |
|---|---|
| `docker compose up redis -d` | Start Redis on port 6379 |
| `mvn clean package -DskipTests -q` | Build the Java jar |
| `java -jar target\tadka-api-0.0.1-SNAPSHOT.jar` | Run the app on port 5224 |
| `k6 run k6/dinner-rush.js` | Load test for rate limiting |
| `curl.exe -N` | SSE stream without buffering |
| `redis-cli` | Inspect Redis keys, pub/sub, lists |

---

## How Redis is wired in Tadka

Redis is **not** "a cache library." It is a **server**. The API is a **client**. Compose starts the server; Java talks to it.

```
GET /restaurants/{id}/menu
        │
        ▼
  ICacheService.getOrSet          GET  restaurant:{id}:menu
        │                              SET  … EX 60          ┐
        ▼                              SET  lock:… NX EX 5   ├─ Redis :6379
  miss → TadkaReadDbContext       PUBLISH order:{id}    ┘
```

**Infra:** `docker-compose.yml` service `redis`, image `redis:7-alpine`, container `tadka-redis`, port **6379**. Volume `redisdata` persists across restarts. Healthcheck is `redis-cli ping`.

**App config:** `application.properties` → `spring.data.redis.host=localhost`, `spring.data.redis.port=6379`, `spring.data.redis.timeout=2000ms`. `cache.mode=` (empty = Redis when available). `rate-limit.algorithm=FixedWindow`, `rate-limit.per-minute=120`, `rate-limit.window-seconds=60`.

**Wiring:** `Day6Config.java` — `@Configuration @EnableAsync`. `@Bean @Primary ICacheService cacheService(...)` returns `RedisCacheService` when Redis is reachable, `InMemoryFallbackCacheService` when `cache.mode=InMemory`, `NullCacheService` otherwise. `@Bean @Primary IRateLimiter rateLimiter(...)` returns `RedisFixedWindowRateLimiter` or `RedisSlidingWindowRateLimiter` or `NullRateLimiter`. `@Bean @Primary IOrderTrackingBus trackingBus(...)` returns `RedisOrderTrackingBus` or `NullOrderTrackingBus`. Explicit `@Bean @ConditionalOnMissingBean(RedisConnectionFactory.class)` creates `LettuceConnectionFactory` and `@Bean @ConditionalOnMissingBean(StringRedisTemplate.class)` creates `StringRedisTemplate`.

**Why not `Spring Cache` abstraction:** `@Cacheable` is GET/SET only. The stampede lock (`SET NX`) and pub/sub need **the same** client. One `RedisTemplate`, three jobs (ADR-018).

### Java infrastructure (what Tadka actually uses)

| Piece | Type | File |
|---|---|---|
| Client library | **Lettuce** (via Spring Data Redis) | `pom.xml` `spring-boot-starter-data-redis` |
| Connection | `LettuceConnectionFactory` bean | `Day6Config.java` 79–84 |
| Cache facade | `ICacheService` (`getOrSet`, `invalidate`) | `infrastructure/caching/ICacheService.java` |
| Cache impl | `RedisCacheService` — `opsForValue().get`/`set` + TTL, `setIfAbsent` lock, `delete` | `RedisCacheService.java` |
| If Redis is down on a **GET** | catch `DataAccessException` → run the DB factory → still HTTP **200** | `RedisCacheService.java` 65–68 |
| Null fallback | `NullCacheService` (always miss), `NullOrderTrackingBus` | `infrastructure/caching/NullCacheService.java` |
| Menu key / TTL | `restaurant:{guid}:menu`, 60 s | `RestaurantsController` → `RestaurantService.getMenu` → `ICacheService.getOrSet` |
| Delete-on-write | after PATCH menu/availability, `invalidate(key)` | `RestaurantsController` 59–66 |
| Live tracking | `IOrderTrackingBus` → `RedisOrderTrackingBus`. Channel `order:{id}`. `isEnabled()` = `redis.ping() == PONG` | `RedisOrderTrackingBus.java` 34–47, 50–71 |
| SSE action | `GET /api/v1/orders/{id}/events` `text/event-stream`, `SseEmitter(5 min)` | `OrderTrackingController.java` 35–85 |
| Replay buffer | Redis LIST `order:{id}:recent` (last 20, 6 h TTL), `Last-Event-ID` header | `RedisOrderTrackingBus.getEventsSince` 79–97 |
| Rate limiter | `RateLimitingMiddleware` (HandlerInterceptor), per-IP, `RedisFixedWindowRateLimiter` (sorted set) | `middleware/RateLimitingMiddleware.java` |
| Redis down on rate limit | `DataAccessException` → fail OPEN (allow request) | `RateLimitingMiddleware.java` 36–39 |
| Conditional GET | `ETagFilterAttribute` — SHA-256 hash of body, `If-None-Match` → 304 | `filters/ETagFilterAttribute.java` |
| Signed URLs | `UrlSigner` — HMAC-SHA256 `{resourceId}|{expiry}`, 5 min default | `infrastructure/security/UrlSigner.java` |
| Invoice controller | `POST /{id}/invoice/sign` → signed URL, `GET /{id}/invoice?sig=&exp=` → invoice JSON | `controller/OrderInvoiceController.java` |

**Menu miss path:** `RestaurantsController.getMenu` → `RestaurantService.getMenu` → `ICacheService.getOrSet`. Redis GET empty → `SET lock:{key} {token} NX EX 5` → replica query (`findByIdWithMenu`) → `SET` JSON + 60 s TTL → delete lock. **Hit path:** Redis GET → deserialize → **no SQL**.

### Same Redis commands, other languages

The **protocol** is Redis. Java and .NET do not get a different cache pattern — they get a different **driver**.

| Job | .NET (reference) | Java | Node |
|---|---|---|---|
| Client library | **StackExchange.Redis** | **Lettuce** (Spring Data Redis) | **ioredis** |
| Process-wide connection | `IConnectionMultiplexer` singleton | `RedisConnectionFactory` / `RedisTemplate` | one Redis client singleton |
| Cache-aside | `GET` → miss → DB → `SET key json EX 60` | same commands, same key shape | same |
| Stampede | `SET lock:{key} token NX EX 5` | `SET … NX EX` | `SET … NX EX` |
| Delete-on-write | `DEL restaurant:{id}:menu` | `DEL` | `DEL` |
| Live tracking | `PUBLISH` / `SUBSCRIBE` `order:{id}` | Lettuce pub/sub (`convertAndSend`) | `publish` / `subscribe` |
| Rate limiting | Sorted set `ratelimit:fixed:{ip}` | same sorted set | same |

If you can run the `redis-cli` file, you can read any of those three codebases: look for GET/SET/DEL/NX/PUBLISH.

---

## 0. Fresh start (pre-class)

**Why wipe:** Day 5 volumes have no Redis. A leftover `tadka-redis` from another clone occupies the name. `down` without `-v` is usually fine for Redis (no volume), but Postgres still wants a clean state when switching branches.

```powershell
git checkout day-06
docker compose down -v
docker rm -f tadka-postgres tadka-redis
docker compose up -d
docker compose ps
docker exec tadka-redis redis-cli PING
```

**What that does:** starts primary and **Redis**. `PING` is the Redis healthcheck you can see. Java migrates the **primary** on first start (same as Day 5); Redis has no schema — Flyway V6 adds performance indexes only.

**How you know you are ready:** `tadka-redis` `(healthy)`. `PONG`. App on **5224**.

Set once in the terminal you will use for Redis + curl (these are PowerShell variables, not Docker):

```powershell
$RID   = "a1b2c3d4-0001-4000-8000-000000000001"
$ITEM   = "b1b2c3d4-0001-4000-8000-000000000001"
$KEY    = "restaurant:${RID}:menu"
$ORDER  = "00000000-0000-0000-0000-000000000001"   # dummy for SSE isEnabled test
```

`$KEY` must expand to `restaurant:a1b2c3d4-0001-4000-8000-000000000001:menu`. `Write-Host $KEY` if unsure.

**Start the app** (from repo root):

```powershell
mvn clean package -DskipTests -q
java -jar target\tadka-api-0.0.1-SNAPSHOT.jar
```

---

## 1. Beat 1 — cache-aside: miss, then hit (ADR-018)

### The story (say this before any command)

```
10,000 people open Meghana's menu
        │
        ▼
   Redis GET  restaurant:{id}:menu
        │
        ├─ HIT  → JSON in ~1 ms, DB never sees it
        └─ MISS → replica SQL, SET key EX 60, return
```

Day 5 made that SQL **fast**. Day 6 asks whether it should **run at all**. Cache only if **repeat + low writes + stale-OK**. Menu yes. Order status / payment **no**.

We **induce a miss** by deleting the key. We do not break the API.

### 1. BREAK — force a miss

**What we did:** delete the cache key so the next GET must hit the DB (replica) and refill Redis.

```powershell
# Remove the menu JSON from Redis. 1 = there was a key, 0 = already empty. Either is fine.
docker exec tadka-redis redis-cli DEL $KEY

# Prove the key is gone. Must be 0. If 1, $KEY did not match (variable not set / quoted wrong).
docker exec tadka-redis redis-cli EXISTS $KEY
```

**How we identified the miss is armed:** `EXISTS` **0**.

### 2. First GET — miss fills Redis

```powershell
# -o NUL hides JSON. time_total is end-to-end HTTP (DB + Redis SET on a miss).
curl.exe -s -o NUL -w "miss  HTTP %{http_code}  time=%{time_total}s`n" http://localhost:5224/api/v1/restaurants/$RID/menu

docker exec tadka-redis redis-cli EXISTS $KEY
docker exec tadka-redis redis-cli TTL $KEY
```

**What that GET does:** `RestaurantsController.getMenu` → `RestaurantService.getMenu` → `ICacheService.getOrSet`. Redis GET empty → lock → replica query → `SET` JSON with 60 s TTL.

**How we identified the miss worked:**

| Output | Meaning |
|---|---|
| HTTP **200** | Menu still served (from DB this time) |
| `EXISTS` **1** | Cache-aside **wrote** Redis |
| `TTL` **~50–60** | Safety net (ADR-018). `-1` = SET without EX (wrong). `-2` = key gone |
| `time=` larger than the next call | Miss pays SQL. Capture cold ~**0.28 s** |

### 3. FIX / proof — second GET is a hit

```powershell
curl.exe -s -o NUL -w "hit   HTTP %{http_code}  time=%{time_total}s`n" http://localhost:5224/api/v1/restaurants/$RID/menu
```

**What it does:** same URL. Redis GET finds JSON. **No SQL.** `RedisCacheService.java` 33–68 hit path.

**Fixed if:** HTTP **200**, `time=` much smaller (capture ~**0.023 s**). Your milliseconds will differ; **hit ≪ miss** is the lesson.

### 4. Scan-count proof — hits do not touch Postgres

The replica serves menu SQL on a miss (`findByIdWithMenu`). If 20 GETs with a warm key still increment scans, the cache is not being used.

```powershell
# seq_scan + idx_scan on menu_items (that is who getMenu queries).
$SQL = "select seq_scan+idx_scan from pg_stat_user_tables where schemaname='public' and relname='menu_items';"
$B = docker exec tadka-postgres psql -U tadka -d tadka -tAc $SQL
1..20 | ForEach-Object { curl.exe -s -o NUL http://localhost:5224/api/v1/restaurants/$RID/menu }
$A = docker exec tadka-postgres psql -U tadka -d tadka -tAc $SQL
Write-Host "before=$B after=$A"
```

**How we identified it worked:** `before` and `after` are **equal** (delta **0**). If after > before: you `DEL`'d mid-loop, Redis was down, or `$RID` was wrong so every GET 404'd / missed.

---

## 2. Beat 2 — delete-on-write (invalidation)

### The story

TTL-only: a cook marks biryani unavailable, customers still see it for **up to 60 s**, then get 409 at checkout. So on **write**, we **delete** the key. Next GET is a miss and refills from DB. TTL is the **safety net** if a delete is missed — not the strategy.

We do **not** write-through (update Redis in the same request as SQL): if Redis SET succeeds and the DB transaction rolls back, the cache **lies**. Delete is safer (worst case is a miss).

### 1. BREAK — PATCH availability while the key exists

Need `EXISTS` 1 first (run Beat 1 if not).

```powershell
# Body is {"isAvailable":false}. Quote @ so PowerShell does not splat the path.
curl.exe -s -w "`nHTTP %{http_code}`n" -X PATCH http://localhost:5224/api/v1/restaurants/$RID/menu/$ITEM/availability -H "Content-Type: application/json" --data-binary '{"isAvailable":false}'

# Must be 0: invalidate() ran after SaveChanges.
docker exec tadka-redis redis-cli EXISTS $KEY
```

**What PATCH does:** `RestaurantsController.updateMenuItemAvailability` → `RestaurantService.updateMenuItemAvailability` → saves to primary → `ICacheService.invalidate($KEY)` → `DEL`. `RestaurantsController.java` 59–66.

**How we identified the issue we just fixed:** if we had skipped `invalidate`, `EXISTS` would stay **1** and GET would still show biryani available. **Identified (good):** HTTP **200** (or 204), `EXISTS` **0**.

### 2. FIX — next GET repopulates

```powershell
curl.exe -s -o NUL -w "HTTP %{http_code} time=%{time_total}s`n" http://localhost:5224/api/v1/restaurants/$RID/menu
docker exec tadka-redis redis-cli EXISTS $KEY
```

**Fixed if:** HTTP **200**, `EXISTS` **1** again. JSON has biryani `isAvailable: false`.

Put the item back so later beats use a normal menu:

```powershell
curl.exe -s -o NUL -X PATCH http://localhost:5224/api/v1/restaurants/$RID/menu/$ITEM/availability -H "Content-Type: application/json" --data-binary '{"isAvailable":true}'
```

**Never cache** order status or payment. Those fail the stale-OK test (Day 4 duplicate order / Day 5 read-your-writes).

---

## 3. Beat 3 — Redis down: same tool, two classifications

### The story

Redis is **one** process doing cache **and** the SSE backplane. When it dies, those two jobs must **fail differently**.

- Menu GET: **performance** dependency. App catches `DataAccessException` and hits the DB. Customer still sees the menu.
- SSE: **correctness** dependency for the stream. No backplane → do not pretend to stream. **503**.

Orders still **place** without Redis (Postgres). Rate limiter **fails open** when Redis is down (ADR-049).

### 1. BREAK — stop Redis, leave the API running

```powershell
# Stops tadka-redis. Does not kill Postgres or the API.
docker compose stop redis

curl.exe -s -o NUL -w "menu HTTP %{http_code} time=%{time_total}s`n" http://localhost:5224/api/v1/restaurants/$RID/menu
curl.exe -s -o NUL -w "sse  HTTP %{http_code}`n" http://localhost:5224/api/v1/orders/$ORDER/events
```

The SSE URL's GUID need not exist: Redis is down, so `isEnabled()` returns `false` → `SseEmitter.completeWithError(...)` → **503** before it cares about the order.

Wait on the menu curl. Capture: menu **200** in ~12 s, SSE **503**. Leftover ADR-049 rate limiter is on every request; this branch **fails it open** when Redis is down so Sunday's beat is not a 500.

**How we identified the two classifications:**

| Output | Meaning |
|---|---|
| menu HTTP **200** | Cache + rate limiter **fail open**. First GET after the stop can take **~5–12 s** (Redis client timeout, then SQL). Do not Ctrl+C. **500** = you are not on this commit |
| sse HTTP **503** body `Live tracking requires Redis` | Stream is a correctness dep. **200 + blank** would lie. `--max-time` if curl hangs |

### 2. FIX — start Redis again

```powershell
docker compose start redis
docker exec tadka-redis redis-cli PING
```

**Fixed if:** `PONG`. Menu GET still 200 (now can hit again). SSE on a **real** order will stream (Beat 5) instead of 503.

---

## 4. Beat 4 — stampede lock (inspect, no 10k herd)

### The story

Cache-aside is fine until a **hot key expires**. At dinner rush, thousands of in-flight menu GETs miss in the same millisecond and **all** hit Postgres. For a moment the DB is busier than with **no** cache.

**Fix (ADR-019):** on miss, `SET lock:{key} {token} NX EX 5`. One winner runs SQL and fills Redis. Losers wait ~80 ms × 5 and re-GET. If still empty, they hit the DB (correctness over purity). Release `DEL`s the lock **only if the token is still ours**.

There is **no** "turn the lock off" lever in this branch. We **read the code** and maybe glimpse the key.

```powershell
# Usually empty: lock lives milliseconds. Empty is NOT "the lock is missing."
docker exec tadka-redis redis-cli KEYS lock:*
```

**How we identified it in code:** `RedisCacheService.java` 42 `setIfAbsent(lockKey, token, 5, SECONDS)` — `When.NotExists` in .NET, `setIfAbsent` in Java. Play the same primitive by hand in [`redis-cli.md` §3](../database/redis-cli.md).

**Honesty:** do not fake a `KEYS` hit. Hot-key (IPL, one restaurant) is **recognize today, solve later** (probabilistic early expiration / stale-while-revalidate — ADR-019 Revisit when).

---

## 5. Beat 5 — SSE live tracking (ADR-020)

### The story

Polling every 2 s is thousands of "nothing changed" SQLs. Tracking is **one-way** (server → phone) → **SSE**, not WebSocket. The stream lives on **one** API process. Two instances: PATCH lands on B, curl is connected to A → **silent drop** unless they share a channel.

**Backplane:** after `SaveChanges`, `OrderStatusChangedEvent` is raised by `Order.transition()` (`Order.java` 114) → dispatched by `DomainEventDispatcher` → `RedisOrderTrackingBus.publishAsync` publishes to Redis channel `order:{id}`. The SSE action **subscribes** first, then writes `event: {Status}` lines until the client disconnects.

Pub/sub is fire-and-forget. Fine for "where is my biryani." Not for payments. Replay/`Last-Event-ID` is **Beat 6**.

### 1. BREAK — stream with no `-N` (optional, 10 seconds)

`curl` without `-N` **buffers**. Terminal A stays blank until you Ctrl+C. Students think SSE is broken.

### 2. FIX — two terminals, `-N` required

**Terminal A** — place an order, copy `"id"`, then stream (leave this running):

```powershell
# POST Priya + Meghana biryani. Copy the JSON "id" GUID. $ORDER is never set.
curl.exe -s -X POST http://localhost:5224/api/v1/orders -H "Content-Type: application/json" --data-binary "@docs\runbooks\place-order.json"

# -N = no buffer. Paste the GUID. Stays open.
curl.exe -N http://localhost:5224/api/v1/orders/PASTE_ID/events
```

**What `-N` does:** disables curl's output buffering so each SSE `event:` line prints when Redis publishes, not at disconnect.

**Terminal B** — same id, confirm the order:

```powershell
# PATCH Created → Confirmed. Inline JSON is fine here.
curl.exe -s -w "`nHTTP %{http_code}`n" -X PATCH http://localhost:5224/api/v1/orders/PASTE_ID/status -H "Content-Type: application/json" --data-binary '{"status":"Confirmed"}'
```

**What B does:** primary `SaveChanges` → `Order.transition(Confirmed)` → `raise(OrderStatusChangedEvent)` → `eventDispatcher.dispatch(...)` → `RedisOrderTrackingBus.publishAsync` → `PUBLISH order:{id}` payload. A is subscribed (`OrderTrackingController.java` 35–85).

**How we identified it worked:**

| Output | Meaning |
|---|---|
| A prints `event: Confirmed` (and a `data:` JSON line) | Backplane delivered. Not polling |
| B HTTP **204** | Status saved even if A is slow |
| A HTTP **503** | Redis still stopped from Beat 3. `compose start redis`, new stream |
| A stays blank | Forgot `-N`, or PASTE_ID mismatch, or Redis down |

Optional miniature (no API): [`redis-cli.md` §5](../database/redis-cli.md) `SUBSCRIBE` / `PUBLISH`.

---

## 6. Beat 6 — reconnect replay buffer (ADR-051)

### The story

SSE connections drop (network blip, client restart). Without a replay buffer, status changes during the disconnection are lost. The client reconnects blind.

**Fix (ADR-051):** `RedisOrderTrackingBus` maintains a capped Redis LIST (`order:{id}:recent`, last 20 events, 6 h TTL) per order. On reconnect, the client sends `Last-Event-ID` (the last sequence ID). `getEventsSince()` reads the buffer and returns events after that ID.

### 1. BREAK — reconnect without replay (no `Last-Event-ID`)

```powershell
# Fresh connect: gets a "current status" event immediately, then waits for new events.
curl.exe -N http://localhost:5224/api/v1/orders/PASTE_ID/events
```

**What happens:** `lastEventId` is `null` → `replayedThrough[0] == 0` → sends current status immediately (`OrderTrackingController.java` 61–68), then polls every 1 s.

### 2. FIX — reconnect with `Last-Event-ID` (replay)

In Terminal A, note the `event:` `id` value (the sequence number). Then reconnect:

```powershell
# Reconnect with Last-Event-ID header to replay missed events.
curl.exe -N http://localhost:5224/api/v1/orders/PASTE_ID/events -H "Last-Event-ID: 5"
```

**What happens:** `lastEventId = "5"` → `Long.parseLong("5")` → `bus.getEventsSince(id, 5)` → reads `order:{id}:recent` LIST → returns events with `seq > 5`. `replayedThrough[0]` advances. New events continue to arrive.

**How we identified it worked:**

| Output | Meaning |
|---|---|
| A prints replayed `event:` lines with increasing `id` | `getEventsSince` found events after the given seq |
| `Last-Event-ID` beyond buffer range | No replay events; current status sent instead. Buffer is 20 events / 6 h |
| A prints nothing new | Reconnect seq was already the latest; waiting for new events |

**Why it matters:** pub/sub is fire-and-forget. Without this buffer, a dropped connection = lost status updates. This is ADR-051, leftover from the .NET branch.

---

## 7. Beat 7 — distributed rate limiting (ADR-049)

### The story

Without rate limiting, a single client can overwhelm the API with requests. Per-instance rate limiting fails when we scale horizontally — a client can distribute requests across instances to bypass limits.

**Fix (ADR-049):** `RedisFixedWindowRateLimiter` uses a Redis sorted set (`ratelimit:fixed:{ip}`) to count requests per IP across all instances. The `RateLimitingMiddleware` intercepts every request and checks the limit before processing.

Redis down → `DataAccessException` → **fail OPEN** (allow the request). The API stays up but is unprotected.

### 1. BREAK — load test with k6

```powershell
k6 run k6\dinner-rush.js
```

**What the script does:** `k6/dinner-rush.js` — stages: 10 VUs for 30 s → 50 VUs for 1 m → ramp down. Each iteration `GET /api/v1/restaurants/a1b2c3d4-0001-4000-8000-000000000001`, sleep 1 s.

**How we identified it worked:**

| Output | Meaning |
|---|---|
| All `status was 200` during ramp-up | Under limit (120/min) |
| Some `status was 429` during 50 VUs peak | Rate limit kicked in |
| Response header `Retry-After: N` | Tells client how many seconds to wait |
| `rate-limit.algorithm=FixedWindow` | Configured in `application.properties` |

### 2. FIX — verify the rate limiter is per-IP and Redis-backed

```powershell
# Check the sorted set exists in Redis for your IP (replace YOUR_IP)
docker exec tadka-redis redis-cli ZCARD "ratelimit:fixed:127.0.0.1"
docker exec tadka-redis redis-cli TTL "ratelimit:fixed:127.0.0.1"
```

**What that proves:** the counter lives in Redis (shared across replicas), not in-process memory. `TTL` ~60 s (the window). After the window expires, the counter resets and requests pass again.

### 3. Alternative — sliding window

```powershell
# In application.properties, change:
# rate-limit.algorithm=SlidingWindow
# Then restart the app.
```

`RedisSlidingWindowRateLimiter` uses the same sorted set with a sliding window for more precise control.

---

## 8. Beat 8 — conditional GET / ETag (ADR-048)

### The story

API responses (menus, orders) contain JSON payloads. Without caching, every request re-sends the full body even when the resource hasn't changed. **ETag** is a hash of the response body. The client sends `If-None-Match` with its cached ETag. On a match, the server returns **304 Not Modified** with an empty body — no bandwidth wasted.

### 1. First GET — capture the ETag

```powershell
curl.exe -s -D - -o NUL http://localhost:5224/api/v1/restaurants/$RID/menu
```

**What to look for:** the response headers include `ETag: "<sha256-hex>"`. This is `ETagFilterAttribute.computeETag` — SHA-256 of the response body, hex-encoded, wrapped in quotes.

### 2. Second GET — conditional request

Copy the ETag value from step 1 (including the quotes):

```powershell
curl.exe -s -D - -o NUL http://localhost:5224/api/v1/restaurants/$RID/menu -H "If-None-Match: \"<copied-etag>\""
```

**How we identified it worked:**

| Output | Meaning |
|---|---|
| HTTP **304** | Body unchanged. Server sent no JSON. Bandwidth saved. |
| HTTP **200** | Body changed (menu updated). Full JSON returned with new ETag. |
| No `ETag` header on first request | The filter is not registered — check `ETagFilterAttribute` is a `@Component` |

### 3. Why this matters

ETag is **stateless** — no server-side session. The hash is computed on every request, but the response body is never sent on a 304. This is ADR-048, carried over from the .NET branch. It complements cache-aside: cache-aside reduces DB hits, ETag reduces bandwidth on cache hits.

---

## 9. Beat 9 — signed URLs (ADR-050)

### The story

API responses (invoices) can be cached at the edge (CDN) to reduce origin load. But standard URL caching is dangerous — anyone can request any URL and get the response. **Signed URLs** use HMAC-SHA256 to authenticate requests, allowing edge caches to serve responses without exposing data to unauthorized clients.

**Fix (ADR-050):** `UrlSigner` generates HMAC-SHA256 signatures over `{resourceId}|{expiryUnixSeconds}`. The `OrderInvoiceController` returns signed URLs. Edge caches can serve the response without re-visiting the origin until the signature expires.

### 1. Request a signed URL

```powershell
curl.exe -s -w "`nHTTP %{http_code}`n" -X POST http://localhost:5224/api/v1/orders/$RID/invoice/sign
```

**What that does:** `OrderInvoiceController.sign` → `orders.findByIdWithItems(id)` → `signer.sign(id.toString(), 5 min)` → returns `SignedInvoiceUrlResponse` with URL like `/api/v1/orders/{id}/invoice?sig=<hex>&exp=<unix-seconds>`.

**What `UrlSigner` does:** `HmacSHA256` over `{id}|{expiresAt}` with the signing key (`Demo:InvoiceSigningKey` env var, default `tadka-demo-signing-key-do-not-use-in-prod`). Hex-encoded, constant-time comparison on verify.

### 2. Fetch the invoice with the signed URL

```powershell
# Copy the full URL from the POST response, including ?sig=...&exp=...
curl.exe -s http://localhost:5224/api/v1/orders/$RID/invoice?sig=<signature>&exp=<expires>
```

**What happens:** `OrderInvoiceController.get` → `signer.verify(id.toString(), exp, sig)` → if valid and not expired → `OrderInvoiceResponse` (JSON). If expired or invalid → **403**.

### 3. Expired signature

Wait past `exp`, or craft a URL with a past `exp`:

```powershell
curl.exe -s -o NUL -w "expired HTTP %{http_code}\n" http://localhost:5224/api/v1/orders/$RID/invoice?sig=bad&exp=1
```

**How we identified it worked:**

| Output | Meaning |
|---|---|
| HTTP **200** + invoice JSON | Signature valid, not expired |
| HTTP **403** | Signature invalid or expired |
| HTTP **400** | Missing `sig` parameter |

**Why it matters:** signed URLs are the standard pattern for secure edge caching. They're simple to implement (HMAC + timestamp), well-supported by CDNs, and don't require session state.

---

## 10. Scale-out preview (ADR-047)

The branch can start **three** API containers behind nginx (`--profile scale-out`). **Do not** teach 047–051 in full. Three questions, then stop:

1. Where does the **cache** live? (Redis = shared. `cache.mode=InMemory` = each box lies.)
2. Who counts **rate limits**? (must be Redis, not per-process memory.)
3. Which box holds the **SSE** connection? (any box, **because** of the backplane.)

```powershell
docker compose --profile scale-out up -d
curl.exe -i http://localhost:8090/health
docker compose --profile scale-out down
```

Beats 4–10 of the break-kit are **homework**. Rate-limit depth is Day 11.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `tadka-redis` name in use | `docker rm -f tadka-redis`; `docker compose up -d` |
| `EXISTS` stays 0 after GET | `$KEY` / `$RID` not set in **this** terminal. `Write-Host $KEY` |
| Scan count moved | You `DEL`'d or Redis was down — hits were misses |
| SSE blank | `curl.exe -N`. Redis `PONG`. 503 = Redis down (by design) |
| SSE 422 | `bus.isEnabled()` returned `false`. Redis not running or `redis-cli ping` not `PONG`. `compose start redis`, new stream |
| SSE `emitter.completeWithError` | `isEnabled()` check at `OrderTrackingController.java` 38–42. Redis down |
| PATCH 400 | Inline JSON or `@file` path wrong. Use `--data-binary '{"status":"Confirmed"}'` |
| Menu 500 with Redis down | You are not on `day-06` (fallback is this branch) |
| `KEYS lock:*` empty | Expected. Read the code; do not fake |
| k6 shows no 429 | Cache is warm (menu already cached). k6 hits `GET /restaurants/{id}` which may already be in Redis from Beat 1. The rate limiter still applies — check `ZCARD ratelimit:fixed:127.0.0.1` |
| `JAVA_HOME` not set for Maven | Set `JAVA_HOME` to the embedded JRE: `C:\Users\khanf\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64` |
| Port 5224 occupied | `taskkill /PID <pid> /F`. Check `netstat -ano | findstr 5224` |
| `RedisConnectionFactory` not found | Clean rebuild: `mvn clean package -DskipTests -q`. `Day6Config.class` not in old jar |
| `isEnabled()` returns false | `redis-cli PING` must return `PONG`. `RedisOrderTrackingBus.isEnabled()` uses `conn.ping()`, not `hasKey("ping")` |
| Rate limiter 429 even at low load | k6 ramping up. Wait for window to expire (60 s). Or check `rate-limit.algorithm` in `application.properties` |
| 304 not returned for ETag | `ETagFilterAttribute` must be registered as `@Component`. Check the `If-None-Match` value exactly matches the `ETag` header (including quotes) |
| Signed URL 403 | Check `exp` is not in the past. Check `sig` matches `HmacSHA256(id|exp, key)`. Verify signing key matches `Demo:InvoiceSigningKey` |

---

## Done when (Sunday)

- [ ] `PONG`; app on **5224**; 28/28
- [ ] Miss then hit (`time_total` down); 20 GETs **+0** Postgres scans
- [ ] PATCH availability → `EXISTS` 0, next GET → 1
- [ ] Redis stopped: menu **200**, SSE **503**
- [ ] Redis started: SSE prints `event: Confirmed` (two terminals)
- [ ] Reconnect with `Last-Event-ID` replays missed events
- [ ] k6 shows 429 with `Retry-After` header
- [ ] `If-None-Match` returns **304**
- [ ] POST `/invoice/sign` returns signed URL; GET with `?sig=&exp=` returns **200**; expired → **403**

---

## References

- ADR-018: Redis cache-aside — `docs/adrs/018-redis-cache-aside.md`
- ADR-019: Cache stampede — `docs/adrs/019-cache-stampede.md`
- ADR-020: Live tracking SSE — `docs/adrs/020-live-tracking-sse.md`
- ADR-047: Stateless scale-out — `docs/adrs/047-stateless-scale-out.md`
- ADR-048: Conditional GET / ETag — `docs/adrs/048-response-compression.md`
- ADR-049: Distributed rate limiting — `docs/adrs/049-distributed-rate-limiting.md`
- ADR-050: Edge cache signed URLs — `docs/adrs/050-edge-cache-signed-urls.md`
- ADR-051: SSE reconnect replay buffer — `docs/adrs/051-sse-reconnect-replay-buffer.md`
- Redis CLI playground: `docs/database/redis-cli.md`
- k6 load tests: `k6/dinner-rush.js`, `k6/polling-vs-sse.js`, `k6/README.md`
- Toydemos: `toydemo/hot-key-stampede-toy/real-redis.js`, `toydemo/stateful-websocket-toy/real-chat.js`
