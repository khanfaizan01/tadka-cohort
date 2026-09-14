-- Day 4 hardening: idempotency keys, coupons, order timestamp columns, xmin concurrency

-- ── Extend ordering.orders with Day 4 timestamp columns ──
ALTER TABLE ordering.orders ADD COLUMN confirmed_at TIMESTAMP;
ALTER TABLE ordering.orders ADD COLUMN cancelled_at TIMESTAMP;
ALTER TABLE ordering.orders ADD COLUMN cancellation_reason VARCHAR(500);

-- xmin is a PostgreSQL system column (xid) that exists on every table.
-- We map it as @Version in JPA; no explicit DDL needed.
-- Unique index on idempotency_keys.key prevents duplicate order creation on replay.

-- ── Idempotency keys ──
CREATE TABLE ordering.idempotency_keys (
    key       VARCHAR(100) PRIMARY KEY,
    order_id  UUID        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_idempotency_keys_order_id ON ordering.idempotency_keys(order_id);

-- ── Coupons (break-kit demo: ADR-045) ──
CREATE TABLE ordering.coupons (
    id              UUID PRIMARY KEY,
    code            VARCHAR(20) NOT NULL UNIQUE,
    max_redemptions INT         NOT NULL,
    redeemed        INT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW()
);

INSERT INTO ordering.coupons (id, code, max_redemptions, redeemed, created_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TADKA50', 100, 0, NOW());

-- ── Coupon redemptions ──
CREATE TABLE ordering.coupon_redemptions (
    id          UUID PRIMARY KEY,
    coupon_id   UUID NOT NULL,
    customer_id UUID NOT NULL,
    redeemed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_coupon_redemptions_coupon FOREIGN KEY (coupon_id) REFERENCES ordering.coupons(id) ON DELETE CASCADE
);
