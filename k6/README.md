# K6 Load Tests

- `dinner-rush.js` — Simulates a dinner rush with increasing load to test rate limiting
- `polling-vs-sse.js` — Compares polling and SSE performance

## Run

```bash
k6 run k6/dinner-rush.js
k6 run k6/polling-vs-sse.js
```
