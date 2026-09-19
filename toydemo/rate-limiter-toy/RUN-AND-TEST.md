# Rate Limiter Toy (Day 6, ADR-049)

## What it demonstrates

Fixed-window vs sliding-window rate limiting. The fixed window has a boundary-burst problem (requests at the edge of a window can double up). The sliding window solves this by tracking each request individually.

## Run

```bash
cd toydemo/rate-limiter-toy
node server.js &
node load-client.js
```

## Expected output

- First 5 requests: 200 OK
- Requests 6-10: 429 Too Many Requests with Retry-After header

## Break kit

Compare with `hot-key-stampede-toy` to see stampede protection vs rate limiting.
