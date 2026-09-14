-- Tadka V3: Seed data for restaurants, menu items, and customer.
-- Column names follow Java @Column / @Embedded @AttributeOverrides exactly.
-- .NET uses 'price'/'currency' and 'latitude'/'longitude'; Java uses
-- 'price_amount'/'price_currency' and 'address_latitude'/'address_longitude'.

-- ── Customer user ──
INSERT INTO identity.users (id, name, email, phone, role, created_at)
VALUES ('c1b2c3d4-0001-4000-8000-000000000001', 'Priya Sharma', 'priya@tadka.test', '+919876500001', 'CUSTOMER', TIMESTAMP '2026-01-01 00:00:00');

-- ── Restaurants ──
INSERT INTO restaurant.restaurants (id, name, address_line1, address_line2, address_city, address_pincode, address_latitude, address_longitude, is_active, avg_prep_time_minutes, created_at)
VALUES
    ('a1b2c3d4-0001-4000-8000-000000000001', 'Meghana Foods', '124, Near Forum Mall', 'Koramangala 5th Block', 'Bangalore', '560095', 12.9352, 77.6245, true, 25, TIMESTAMP '2026-01-01 00:00:00'),
    ('a1b2c3d4-0002-4000-8000-000000000002', 'Truffles', '96, 12th Main Road', 'HAL 2nd Stage, Indiranagar', 'Bangalore', '560038', 12.9784, 77.6408, true, 30, TIMESTAMP '2026-01-01 00:00:00'),
    ('a1b2c3d4-0003-4000-8000-000000000003', 'Vidyarthi Bhavan', '32, Gandhi Bazaar Main Road', 'Basavanagudi', 'Bangalore', '560004', 12.9454, 77.5726, true, 20, TIMESTAMP '2026-01-01 00:00:00');

-- ── Menu items (16 items across 3 restaurants) ──
-- Meghana Foods (restaurant_id = a1b2c3d4-0001...)
INSERT INTO restaurant.menu_items (id, name, description, price_amount, price_currency, category, is_available, is_veg, restaurant_id)
VALUES
    ('b1b2c3d4-0001-4000-8000-000000000001', 'Chicken Biryani', 'Hyderabadi-style dum biryani with tender chicken', 299, 'INR', 'Biryani', true, false, 'a1b2c3d4-0001-4000-8000-000000000001'),
    ('b1b2c3d4-0002-4000-8000-000000000002', 'Mutton Biryani', 'Slow-cooked mutton dum biryani with salan', 399, 'INR', 'Biryani', true, false, 'a1b2c3d4-0001-4000-8000-000000000001'),
    ('b1b2c3d4-0003-4000-8000-000000000003', 'Paneer Butter Masala', 'Creamy paneer in rich tomato gravy', 249, 'INR', 'Main Course', true, true, 'a1b2c3d4-0001-4000-8000-000000000001'),
    ('b1b2c3d4-0004-4000-8000-000000000004', 'Gutti Vankaya', 'Stuffed brinjal curry, Andhra style', 199, 'INR', 'Main Course', true, true, 'a1b2c3d4-0001-4000-8000-000000000001'),
    ('b1b2c3d4-0005-4000-8000-000000000005', 'Chicken 65', 'Spicy deep-fried chicken, Hyderabadi classic', 229, 'INR', 'Starters', true, false, 'a1b2c3d4-0001-4000-8000-000000000001'),
    ('b1b2c3d4-0006-4000-8000-000000000006', 'Curd Rice', 'Comfort food with tempered curd rice', 99, 'INR', 'Rice', true, true, 'a1b2c3d4-0001-4000-8000-000000000001');

-- Truffles (restaurant_id = a1b2c3d4-0002...)
INSERT INTO restaurant.menu_items (id, name, description, price_amount, price_currency, category, is_available, is_veg, restaurant_id)
VALUES
    ('b1b2c3d4-0007-4000-8000-000000000007', 'Classic Smash Burger', 'Double-patty smash burger with house sauce', 299, 'INR', 'Burgers', true, false, 'a1b2c3d4-0002-4000-8000-000000000002'),
    ('b1b2c3d4-0008-4000-8000-000000000008', 'Truffle Special Burger', 'Signature burger with truffle mayo and caramelized onions', 449, 'INR', 'Burgers', true, false, 'a1b2c3d4-0002-4000-8000-000000000002'),
    ('b1b2c3d4-0009-4000-8000-000000000009', 'Loaded Fries', 'Crispy fries with cheese, jalapenos, and sour cream', 199, 'INR', 'Sides', true, true, 'a1b2c3d4-0002-4000-8000-000000000002'),
    ('b1b2c3d4-000a-4000-8000-000000000010', 'Chocolate Shake', 'Thick chocolate milkshake with whipped cream', 179, 'INR', 'Beverages', true, true, 'a1b2c3d4-0002-4000-8000-000000000002'),
    ('b1b2c3d4-000b-4000-8000-000000000011', 'Grilled Chicken Sandwich', 'Grilled chicken breast with lettuce and garlic aioli', 279, 'INR', 'Sandwiches', true, false, 'a1b2c3d4-0002-4000-8000-000000000002');

-- Vidyarthi Bhavan (restaurant_id = a1b2c3d4-0003...)
INSERT INTO restaurant.menu_items (id, name, description, price_amount, price_currency, category, is_available, is_veg, restaurant_id)
VALUES
    ('b1b2c3d4-000c-4000-8000-000000000012', 'Masala Dosa', 'Crispy dosa with spiced potato filling', 80, 'INR', 'Dosa', true, true, 'a1b2c3d4-0003-4000-8000-000000000003'),
    ('b1b2c3d4-000d-4000-8000-000000000013', 'Benne Masala Dosa', 'Butter-roasted dosa, Karnataka specialty', 99, 'INR', 'Dosa', true, true, 'a1b2c3d4-0003-4000-8000-000000000003'),
    ('b1b2c3d4-000e-4000-8000-000000000014', 'Idli Vada', 'Steamed idli with crispy medu vada and sambar', 60, 'INR', 'Breakfast', true, true, 'a1b2c3d4-0003-4000-8000-000000000003'),
    ('b1b2c3d4-000f-4000-8000-000000000015', 'Kesari Bath', 'Sweet semolina halwa with ghee and cashews', 50, 'INR', 'Desserts', true, true, 'a1b2c3d4-0003-4000-8000-000000000003'),
    ('b1b2c3d4-0010-4000-8000-000000000016', 'Filter Coffee', 'South Indian filter coffee, strong and frothy', 30, 'INR', 'Beverages', true, true, 'a1b2c3d4-0003-4000-8000-000000000003');
