# Stateful WebSocket Toy (Day 6)

## What it demonstrates

How a stateless service can be scaled out while maintaining sticky connections. The toy shows the problem with stateful WebSocket connections and how Redis can serve as a shared backplane.

## Run

```bash
cd toydemo/stateful-websocket-toy
node index.js &
# Connect with: wscat -c ws://localhost:3003
```

## Break kit

Compare with `redis-cli-playground` to see the difference between stateful connections and a shared backplane.
