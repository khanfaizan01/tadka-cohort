# Hot Key Stampede Toy (Day 6, ADR-019)

## What it demonstrates

Cache stampede: when a popular cache key expires, N concurrent requests all miss the cache and hit the DB simultaneously. The single-flight stampede protection ensures only ONE request refreshes the cache while others wait.

## Run

```bash
cd toydemo/hot-key-stampede-toy
node index.js &
# Then send concurrent requests to the same key
```

## Break kit

Compare with `rate-limiter-toy` to see rate limiting vs stampede protection.
