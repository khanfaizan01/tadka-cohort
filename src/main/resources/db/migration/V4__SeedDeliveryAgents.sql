-- Tadka V4: Seed delivery agents.
-- Java entity DeliveryAgent uses @Embedded GeoLocation →
-- columns: current_location_latitude, current_location_longitude
-- (.NET uses current_latitude / current_longitude — different names.)

INSERT INTO delivery.delivery_agents (id, name, phone, status, current_location_latitude, current_location_longitude)
VALUES
    ('d1b2c3d4-0001-4000-8000-000000000001', 'Ramesh Kumar', '+919876543210', 'Available', 12.9352, 77.6245),
    ('d1b2c3d4-0002-4000-8000-000000000002', 'Suresh Patel', '+919876543211', 'Available', 12.9784, 77.6408);
