# Database Schema

Five schemas in a single PostgreSQL 16 instance. Flyway migrations manage DDL.

## Schema map

| Schema | Tables | Purpose |
|--------|--------|---------|
| `identity` | `users`, `user_addresses` | Customer/identity management |
| `restaurant` | `restaurants`, `menu_items` | Restaurant catalog and menus |
| `ordering` | `orders`, `order_items`, `coupons`, `coupon_redemptions`, `idempotency_keys` | Order lifecycle and coupons |
| `delivery` | `delivery_agents`, `delivery_assignments` | Delivery agent management and assignments |
| `payment` | `payments` | Payment processing |

## Entity mapping

All JPA entities use `@Table(schema = "...")`. Value objects (`Money`, `Address`, `GeoLocation`) are `@Embeddable` and stored as columns.

### `ordering.orders`

Columns: `id`, `customer_id`, `restaurant_id`, `status`, `total_amount_amount`, `total_amount_currency`, `delivery_address_line1`, `delivery_address_line2`, `delivery_address_city`, `delivery_address_pincode`, `delivery_address_latitude`, `delivery_address_longitude`, `created_at`, `confirmed_at`, `cancelled_at`, `cancellation_reason`, `xmin`.

- `customer_id` → `identity.users.id` (no FK, ADR-008)
- `restaurant_id` → `restaurant.restaurants.id` (no FK, ADR-008)
- `xmin` is a PostgreSQL system column mapped as `@Version` for optimistic concurrency (ADR-012)
- `confirmed_at`, `cancelled_at`, `cancellation_reason` added in V2__Day4_Hardening

### `ordering.order_items`

Columns: `id`, `order_id`, `menu_item_id`, `name`, `quantity`, `unit_price_amount`, `unit_price_currency`, `special_instructions`.

- `order_id` → `ordering.orders.id` (FK, same schema)
- `menu_item_id` → `restaurant.menu_items.id` (no FK — snapshot only, ADR-009)
- `name` and `unit_price_*` are snapshots at order time

### `restaurant.menu_items`

Columns: `id`, `name`, `description`, `price_amount`, `price_currency`, `category`, `is_available`, `is_veg`, `restaurant_id`.

- `restaurant_id` → `restaurant.restaurants.id` (FK, same schema)
- `price_amount`/`price_currency` stored as columns (Money as `@Embeddable`)

## Cross-schema constraints

No foreign keys cross schema boundaries. Cross-domain references use UUID identifiers validated in application code (ADR-008).

Prove it:

```sql
SELECT tc.table_schema, tc.table_name, ccu.table_schema AS foreign_schema
FROM information_schema.table_constraints tc
JOIN information_schema.constraint_column_usage ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema <> ccu.table_schema;
-- Expected: 0 rows
```
