# Redis CLI Playground

## Connecting

```bash
redis-cli -h localhost -p 6379
```

Or with Docker:
```bash
docker exec -it tadka-redis redis-cli
```

## Strings

```bash
# Set and get
SET mykey "Hello World"
GET mykey

# TTL
TTL mykey
EXPIRE mykey 60

# Cache-aside pattern keys (from the app)
GET lock:mykey
GET order:{id}:seq
```

## Hash

```bash
# Store structured data
HSET user:1001 name "Alice" role "customer"
HGET user:1001 name
HGETALL user:1001
```

## List

```bash
# Recent events buffer (from the app)
LRANGE order:{id}:recent 0 -1
LLEN order:{id}:recent
RPUSH order:{id}:recent "event1"
```

## Set

```bash
# Unique collections
SADD coupons:active "SUMMER20" "WELCOME10"
SMEMBERS coupons:active
SISMEMBER coupons:active "SUMMER20"
```

## Sorted Set

```bash
# Rate limiting (sliding window)
ZCARD ratelimit:sliding:127.0.0.1
ZRANGE ratelimit:sliding:127.0.0.1 0 0 WITHSCORES
ZREMRANGEBYSCORE ratelimit:sliding:127.0.0.1 0 -1
```

## Pub/Sub

```bash
# Subscribe to order events
SUBSCRIBE order:{id}

# Publish to a channel
PUBLISH order:{id} '{"seq":1,"status":"Confirmed"}'
```

## Keys

```bash
# Find all keys matching a pattern
KEYS order:*
KEYS ratelimit:*

# Delete a key
DEL mykey

# Flush all (DEV ONLY)
FLUSHALL
```

## Monitoring

```bash
# Watch live commands
MONITOR

# Info about clients
CLIENT LIST

# Memory usage
INFO memory

# Info about the server
INFO server
```

## Debugging the Tadka App

```bash
# Check if Redis is connected
redis-cli ping
# → PONG

# Check recent events for an order
LRANGE order:{id}:recent 0 -1

# Check the sequence number
GET order:{id}:seq

# Check rate limiting state
ZRANGE ratelimit:sliding:127.0.0.1 0 -1 WITHSCORES

# Check cache contents
GET lock:mykey
```
