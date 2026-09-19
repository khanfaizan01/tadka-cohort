# ADR-020: Live Order Tracking via SSE (ADR-020)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Provide real-time order status updates to clients using Server-Sent Events (SSE).

**Context:** Customers and delivery agents need live visibility into order status changes (confirmed → cooking → dispatched → delivered). Polling is wasteful (constant HTTP requests, high server load). WebSockets are overkill for one-directional status updates. SSE provides a lightweight, HTTP-based, one-directional streaming channel.

**Options:**
1. **Polling:** Client polls `/orders/{id}/status` every N seconds.
2. **WebSockets:** Full-duplex WebSocket connection.
3. **SSE (Server-Sent Events):** One-directional streaming from server to client over HTTP.
4. **Redis Pub/Sub + SSE:** Redis pub/sub as the backplane; SSE as the client-facing protocol.

**Choice:** Option 4 — Redis pub/sub + SSE. The `IOrderTrackingBus` interface abstracts the backplane. `RedisOrderTrackingBus` uses Redis pub/sub to broadcast status changes. `OrderTrackingController` uses `SseEmitter` to stream events to clients.

**Why:** SSE is native HTTP (works through proxies, reconnects automatically, uses standard HTTP status codes). Redis pub/sub provides the shared backplane for horizontal scaling — any server instance can publish a status change, and all connected SSE streams receive it. The `NullOrderTrackingBus` fallback returns 422 "Live tracking requires Redis" when Redis is unavailable.

**Trade-off:** SSE is one-directional (server → client). For bidirectional (client → server), WebSockets are still needed. **At-most-once:** if the Redis pub/sub message is lost (network partition), the client misses the event. Reconnect replays the last 20 events from a Redis LIST buffer.

**Failure mode:** Redis is down → `NullOrderTrackingBus` → SSE returns 422. The rest of the API stays functional. The app degrades gracefully.

**Revisit when:** We need bidirectional real-time (e.g., live map tracking). Then consider WebSockets with Redis as a shared state store.

**References:**
- ADR-051: SSE reconnect replay buffer
- `toydemo/stateful-websocket-toy/real-chat.js`
