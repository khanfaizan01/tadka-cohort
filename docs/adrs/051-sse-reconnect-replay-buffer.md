# ADR-051: SSE Reconnect Replay Buffer (ADR-051)

**Date:** 2026-09-15
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** Enable SSE clients to replay missed events after reconnection.

**Context:** When an SSE connection drops (network issue, client restart), the client reconnects but has no way to know what events it missed. Without a replay buffer, status changes during the disconnection are lost. The client would need to poll the full order state, which defeats the purpose of SSE.

**Options:**
1. **Full state on reconnect:** Client requests full order state on reconnect; SSE only sends new events after.
2. **Redis LIST replay buffer:** Store the last N events in a Redis LIST; on reconnect, send events after the client's last known sequence ID.
3. **Event sourcing:** Store all events in an event store; replay from the last sequence.

**Choice:** Option 2 — Redis LIST replay buffer. `RedisOrderTrackingBus` maintains a capped Redis LIST (`order:{id}:recent`, last 20 events, 6h TTL) per order. On reconnect, the client sends `Last-Event-ID` (the last sequence ID). `getEventsSince()` reads the buffer and returns events after that ID.

**Why:** Redis LIST is simple, fast, and naturally fits the capped-recent-events pattern. The 6h TTL ensures old events are auto-expired. The 20-event cap limits memory usage. `Last-Event-ID` is a simple integer sequence number, easy for clients to implement.

**Trade-off:** Events older than 6h or beyond the 20-event cap cannot be replayed. Clients that reconnect after >6h will get a "current state" event (no missed events). **Memory:** 20 events × small payloads per order = minimal Redis memory.

**Failure mode:** Redis is down → `NullOrderTrackingBus` → no replay buffer → SSE returns 422. No live tracking, but the API stays functional.

**Revisit when:** We need replay of events older than 6h or more than 20 events. Then consider an event store or long-term Redis stream.

**References:**
- ADR-020: Live order tracking via SSE
- `RedisOrderTrackingBus.getEventsSince()`
