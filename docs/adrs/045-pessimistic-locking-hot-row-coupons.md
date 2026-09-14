# ADR-045: Pessimistic Locking for Hot-Row Coupons

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** During a flash sale, thousands of customers try to redeem the same coupon code simultaneously. How do we prevent overselling?

**Options:**
1. Optimistic locking with retry (the `xmin` pattern from ADR-012).
2. Pessimistic locking: `SELECT ... FOR UPDATE` or `FOR SHARE` on the coupon row.
3. Redis rate limiter before the DB write.
4. A dedicated queue with serialized processing.

**Choice:** Option 2 for the coupon redemption path. `SELECT ... FOR UPDATE` locks the `ordering.coupons` row during redemption. `FOR WAIT` blocks until the lock is available (default). `FOR NOWAIT` fails immediately if locked. `FOR SKIP LOCKED` lets the next customer take the row.

**Why:** Coupons are a classic hot-row problem — thousands of concurrent requests hit one row. Optimistic locking (ADR-012) generates many retries under contention; pessimistic locking serializes access at the DB level. This is ADR-012's "exception case" — a single row with many writers.

**Trade-off:** `FOR UPDATE` blocks other transactions trying to lock the same row, increasing latency. Under extreme load, connections pile up waiting for the lock. The lock must be held only for the minimal duration (one UPDATE, not an HTTP call).

**Failure mode:** Holding `FOR UPDATE` across an HTTP gateway call (payment confirmation) locks the row for the full network latency — the whole app sits. Or optimistic locking on the coupon generates thousands of retries, wasting CPU.

**Revisit when:** Coupon contention becomes measurable (p99 latency spikes). Then `FOR SKIP LOCKED` or a queue-based approach is the next step. This ADR is the teaching hook for ADR-012's limits.
