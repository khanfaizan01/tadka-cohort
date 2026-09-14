-- Tadka Day 1–3: Schema creation and tables (ADR-003, ADR-008)
-- Five schemas in a single PostgreSQL 16 instance.
-- No cross-schema foreign keys (ADR-008). All cross-domain references use UUIDs.

CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS restaurant;
CREATE SCHEMA IF NOT EXISTS ordering;
CREATE SCHEMA IF NOT EXISTS delivery;
CREATE SCHEMA IF NOT EXISTS payment;

-- ── identity schema ────────────────────────────────────────────────────────

CREATE TABLE identity.users (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    email VARCHAR NOT NULL,
    phone VARCHAR,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_users_email ON identity.users(email);

CREATE TABLE identity.user_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    label VARCHAR,
    address_line1 VARCHAR,
    address_line2 VARCHAR,
    address_city VARCHAR,
    address_pincode VARCHAR,
    address_latitude DOUBLE PRECISION,
    address_longitude DOUBLE PRECISION,
    is_default BOOLEAN NOT NULL,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES identity.users(id) ON DELETE CASCADE
);

-- ── restaurant schema ──────────────────────────────────────────────────────

CREATE TABLE restaurant.restaurants (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    address_line1 VARCHAR,
    address_line2 VARCHAR,
    address_city VARCHAR,
    address_pincode VARCHAR,
    address_latitude DOUBLE PRECISION,
    address_longitude DOUBLE PRECISION,
    is_active BOOLEAN NOT NULL,
    avg_prep_time_minutes INTEGER,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE restaurant.menu_items (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    description VARCHAR,
    price_amount NUMERIC,
    price_currency VARCHAR,
    category VARCHAR,
    is_available BOOLEAN NOT NULL,
    is_veg BOOLEAN NOT NULL,
    restaurant_id UUID NOT NULL,
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant.restaurants(id) ON DELETE CASCADE
);

-- ── ordering schema ────────────────────────────────────────────────────────

CREATE TABLE ordering.orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount_amount NUMERIC,
    total_amount_currency VARCHAR,
    delivery_address_line1 VARCHAR,
    delivery_address_line2 VARCHAR,
    delivery_address_city VARCHAR,
    delivery_address_pincode VARCHAR,
    delivery_address_latitude DOUBLE PRECISION,
    delivery_address_longitude DOUBLE PRECISION,
    created_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP
);

CREATE TABLE ordering.order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    menu_item_id UUID NOT NULL,
    name VARCHAR NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price_amount NUMERIC,
    unit_price_currency VARCHAR,
    special_instructions VARCHAR,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES ordering.orders(id) ON DELETE CASCADE
);

-- ── delivery schema ────────────────────────────────────────────────────────

CREATE TABLE delivery.delivery_agents (
    id UUID PRIMARY KEY,
    name VARCHAR NOT NULL,
    phone VARCHAR NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_location_latitude DOUBLE PRECISION,
    current_location_longitude DOUBLE PRECISION
);

CREATE TABLE delivery.delivery_assignments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    agent_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    picked_up_at TIMESTAMP,
    delivered_at TIMESTAMP
);

-- ── payment schema ─────────────────────────────────────────────────────────

CREATE TABLE payment.payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount_amount NUMERIC,
    amount_currency VARCHAR,
    method VARCHAR NOT NULL,
    status VARCHAR(20) NOT NULL,
    gateway_reference VARCHAR,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);
