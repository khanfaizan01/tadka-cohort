INSERT INTO identity.users (id, name, email, phone, role, created_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Test Customer', 'test@example.com', '5550000', 'CUSTOMER', NOW());

INSERT INTO restaurant.restaurants (id, name, address_line1, address_city, is_active, avg_prep_time_minutes, created_at)
VALUES ('6ba7b810-9dad-11d1-80b4-00c04fd430c8', 'Test Restaurant', '123 Main St', 'Springfield', true, 20, NOW());
