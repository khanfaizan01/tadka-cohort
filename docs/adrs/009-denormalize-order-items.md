# ADR-009: Snapshot Name and Price on Order Lines

**Date:** 2026-08-28
**Status:** Accepted
**Deciders:** Architecture team / Tadka cohort

**Topic:** When an order is placed, should `order_items` store a copy of the menu item's name and price, or reference the live menu?

**Options:**
1. Reference the menu item by ID; always read the current price from `menu_items`.
2. Snapshot `name` and `unit_price` onto each `order_item` row at order time.
3. Reference, but add a `historical_price` column on `order_items`.

**Choice:** Option 2. `order_items.name` and `order_items.unit_price_amount`/`unit_price_currency` are snapshots. The `OrderFactory.create()` reads the restaurant's menu at order time and copies these fields onto each `OrderItem`.

**Why:** Menu prices change (a biryani that was ₹14.99 can become ₹16.99 next month). Historical accuracy requires the price *as it was when ordered*. Querying the live menu for an old order gives the wrong number. Snapshotting is cheap — an extra two columns on a child table.

**Trade-off:** `order_items` now has denormalized data that must stay in sync at write time. If a restaurant corrects a menu item name retroactively, the order snapshot won't reflect it. This is acceptable — orders are immutable historical records.

**Failure mode:** Not snapshotting means a customer sees a different total on their receipt than what they agreed to pay. Or an admin changes a price and suddenly all historical revenue reports are wrong.

**Revisit when:** The menu-item lookup becomes a cross-service call (Restaurant extracted). Then a local read model with a cached snapshot is the only option, which is essentially this ADR.
