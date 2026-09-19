-- Day 6: Performance indexes for scaling
-- ADR-014: Indexing strategy

-- Customer order history query (used by GET /orders?customerId=)
CREATE INDEX IF NOT EXISTS idx_orders_customer_created ON ordering.orders (customer_id, created_at DESC);

-- Order status tracking query (used by live tracking)
CREATE INDEX IF NOT EXISTS idx_orders_status ON ordering.orders (status);

-- Idempotency key lookup
CREATE INDEX IF NOT EXISTS idx_idempotency_keys_key ON ordering.idempotency_keys ("key");

-- Coupon redemption tracking
CREATE INDEX IF NOT EXISTS idx_coupons_code ON ordering.coupons (code);
CREATE INDEX IF NOT EXISTS idx_coupon_redemptions_coupon ON ordering.coupon_redemptions (coupon_id, redeemed_at DESC);
