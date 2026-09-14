-- ============================================================
-- Tadka API — Dummy Data for Testing
-- Truncate and re-insert all app tables in FK-safe order
-- ============================================================

TRUNCATE payment.payments CASCADE;
TRUNCATE delivery.delivery_assignments CASCADE;
TRUNCATE delivery.delivery_agents CASCADE;
TRUNCATE ordering.coupon_redemptions CASCADE;
TRUNCATE ordering.idempotency_keys CASCADE;
TRUNCATE ordering.order_items CASCADE;
TRUNCATE ordering.orders CASCADE;
TRUNCATE ordering.coupons CASCADE;
TRUNCATE restaurant.menu_items CASCADE;
TRUNCATE identity.user_addresses CASCADE;
TRUNCATE identity.users CASCADE;
TRUNCATE restaurant.restaurants CASCADE;

-- ── IDENTITY ──
INSERT INTO identity.users (id, name, email, phone, role, created_at) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Alice Johnson', 'alice@example.com', '555-0101', 'CUSTOMER', NOW()),
  ('22222222-2222-2222-2222-222222222222', 'Bob Smith', 'bob@example.com', '555-0102', 'CUSTOMER', NOW()),
  ('33333333-3333-3333-3333-333333333333', 'Carol Admin', 'carol@example.com', '555-0103', 'ADMIN', NOW());

INSERT INTO identity.user_addresses (id, user_id, label, address_line1, address_line2, address_city, address_pincode, address_latitude, address_longitude, is_default) VALUES
  ('1a111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', 'Home', '123 Main St', 'Apt 4B', 'Springfield', '62704', 39.7817, -89.6501, true),
  ('1a111111-1111-1111-1111-111111111112', '22222222-2222-2222-2222-222222222222', 'Work', '456 Oak Ave', '', 'Springfield', '62704', 39.7817, -89.6501, false);

-- ── RESTAURANT ──
INSERT INTO restaurant.restaurants (id, name, address_line1, address_line2, address_city, address_pincode, address_latitude, address_longitude, is_active, avg_prep_time_minutes, created_at) VALUES
  ('44444444-4444-4444-4444-444444444444', 'Tadka North', '789 Spice Rd', '', 'Springfield', '62701', 39.7817, -89.6501, true, 20, NOW()),
  ('55555555-5555-5555-5555-555555555555', 'Curry House', '321 Masala Blvd', '', 'Springfield', '62702', 39.7917, -89.6601, true, 25, NOW()),
  ('66666666-6666-6666-6666-666666666666', 'Biryani Palace', '555 Biryani Ln', '', 'Champaign', '61820', 40.0017, -88.2011, true, 30, NOW());

INSERT INTO restaurant.menu_items (id, name, description, price_amount, price_currency, category, is_available, is_veg, restaurant_id) VALUES
  ('77777777-7777-7777-7777-777777777777', 'Chicken Tikka', 'Smoked chicken in tandoori spices', 12.99, 'INR', 'Main Course', true, false, '44444444-4444-4444-4444-444444444444'),
  ('88888888-8888-8888-8888-888888888888', 'Paneer Butter Masala', 'Cottage cheese in creamy tomato sauce', 9.99, 'INR', 'Main Course', true, true, '44444444-4444-4444-4444-444444444444'),
  ('99999999-9999-9999-9999-999999999999', 'Chicken Biryani', 'Aromatic basmati rice with chicken', 14.99, 'INR', 'Main Course', true, false, '55555555-5555-5555-5555-555555555555'),
  ('aaaaaaa1-1111-1111-1111-111111111111', 'Vegetable Biryani', 'Mixed vegetables in fragrant rice', 11.99, 'INR', 'Main Course', true, true, '55555555-5555-5555-5555-555555555555'),
  ('bbbbbbb2-2222-2222-2222-222222222222', 'Garlic Naan', 'Fresh baked garlic bread', 3.99, 'INR', 'Side', true, true, '44444444-4444-4444-4444-444444444444');

-- ── ORDERING ──
INSERT INTO ordering.orders (id, customer_id, restaurant_id, status, total_amount_amount, total_amount_currency, delivery_address_line1, delivery_address_line2, delivery_address_city, delivery_address_pincode, delivery_address_latitude, delivery_address_longitude, created_at, confirmed_at, delivered_at) VALUES
  ('cccccccc-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111', '44444444-4444-4444-4444-444444444444', 'CONFIRMED', 22.98, 'INR', '123 Main St', 'Apt 4B', 'Springfield', '62704', 39.7817, -89.6501, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour', NULL),
  ('cccccccc-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222', '55555555-5555-5555-5555-555555555555', 'DELIVERED', 26.98, 'INR', '456 Oak Ave', '', 'Springfield', '62704', 39.7817, -89.6501, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '1 hour'),
  ('cccccccc-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111', '66666666-6666-6666-6666-666666666666', 'PREPARING', 11.99, 'INR', '123 Main St', 'Apt 4B', 'Springfield', '62704', 39.7817, -89.6501, NOW() - INTERVAL '30 minutes', NULL, NULL);

INSERT INTO ordering.order_items (id, order_id, menu_item_id, name, quantity, unit_price_amount, unit_price_currency, special_instructions) VALUES
  ('dddd1111-1111-1111-1111-111111111111', 'cccccccc-1111-1111-1111-111111111111', '77777777-7777-7777-7777-777777777777', 'Chicken Tikka', 2, 12.99, 'INR', 'Extra spicy'),
  ('dddd1111-1111-1111-1111-111111111112', 'cccccccc-1111-1111-1111-111111111111', 'bbbbbbb2-2222-2222-2222-222222222222', 'Garlic Naan', 1, 3.99, 'INR', NULL),
  ('dddd2222-2222-2222-2222-222222222222', 'cccccccc-2222-2222-2222-222222222222', '99999999-9999-9999-9999-999999999999', 'Chicken Biryani', 1, 14.99, 'INR', NULL),
  ('dddd2222-2222-2222-2222-222222222223', 'cccccccc-2222-2222-2222-222222222222', 'aaaaaaa1-1111-1111-1111-111111111111', 'Vegetable Biryani', 1, 11.99, 'INR', NULL),
  ('dddd3333-3333-3333-3333-333333333333', 'cccccccc-3333-3333-3333-333333333333', 'aaaaaaa1-1111-1111-1111-111111111111', 'Vegetable Biryani', 1, 11.99, 'INR', NULL);

INSERT INTO ordering.coupons (id, code, max_redemptions, redeemed, created_at) VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TADKA50', 100, 12, NOW() - INTERVAL '30 days'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'TADKA25', 200, 45, NOW() - INTERVAL '60 days'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'FIRSTORDER', 500, 210, NOW() - INTERVAL '90 days');

INSERT INTO ordering.coupon_redemptions (id, coupon_id, customer_id, redeemed_at) VALUES
  ('eeee1111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', NOW() - INTERVAL '10 days'),
  ('eeee2222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', NOW() - INTERVAL '5 days'),
  ('eeee3333-3333-3333-3333-333333333333', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', NOW() - INTERVAL '2 days');

INSERT INTO ordering.idempotency_keys (key, order_id, created_at) VALUES
  ('idem-key-001', 'cccccccc-1111-1111-1111-111111111111', NOW() - INTERVAL '2 hours'),
  ('idem-key-002', 'cccccccc-2222-2222-2222-222222222222', NOW() - INTERVAL '5 hours'),
  ('idem-key-003', 'cccccccc-3333-3333-3333-333333333333', NOW() - INTERVAL '30 minutes');

-- ── DELIVERY ──
INSERT INTO delivery.delivery_agents (id, name, phone, status, current_location_latitude, current_location_longitude) VALUES
  ('ffffff11-1111-1111-1111-111111111111', 'Raj Kumar', '555-1001', 'ACTIVE', 39.7817, -89.6501),
  ('ffffff22-2222-2222-2222-222222222222', 'Priya Singh', '555-1002', 'ACTIVE', 39.7917, -89.6601),
  ('ffffff33-3333-3333-3333-333333333333', 'Amit Patel', '555-1003', 'OFFLINE', 39.7717, -89.6401);

INSERT INTO delivery.delivery_assignments (id, order_id, agent_id, status, assigned_at, picked_up_at, delivered_at) VALUES
  ('ffffffff-1111-1111-1111-111111111111', 'cccccccc-2222-2222-2222-222222222222', 'ffffff11-1111-1111-1111-111111111111', 'DELIVERED', NOW() - INTERVAL '4 hours', NOW() - INTERVAL '2.5 hours', NOW() - INTERVAL '1 hour'),
  ('ffffffff-2222-2222-2222-222222222222', 'cccccccc-1111-1111-1111-111111111111', 'ffffff22-2222-2222-2222-222222222222', 'ASSIGNED', NOW() - INTERVAL '1 hour', NULL, NULL);

-- ── PAYMENT ──
INSERT INTO payment.payments (id, order_id, amount_amount, amount_currency, method, status, gateway_reference, created_at, completed_at) VALUES
  ('12345678-1111-1111-1111-111111111111', 'cccccccc-1111-1111-1111-111111111111', 22.98, 'INR', 'UPI', 'COMPLETED', 'txn_upi_001', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour'),
  ('12345678-2222-2222-2222-222222222222', 'cccccccc-2222-2222-2222-222222222222', 26.98, 'INR', 'CARD', 'COMPLETED', 'txn_card_002', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '1 hour'),
  ('12345678-3333-3333-3333-333333333333', 'cccccccc-3333-3333-3333-333333333333', 11.99, 'INR', 'UPI', 'PENDING', NULL, NOW() - INTERVAL '30 minutes', NULL);
