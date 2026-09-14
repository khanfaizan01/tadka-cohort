# Day 3 — Request Flow

The full request flow for placing an order.

```mermaid
sequenceDiagram
    participant C as Client
    participant R as OrdersController
    participant F as OrderFactory
    participant S as OrderService
    participant Repo as OrderRepository
    participant DB as PostgreSQL

    C->>R: POST /api/v1/orders { customerId, restaurantId, items }
    R->>S: placeOrder(customerId, restaurantId, items, address, idempotencyKey)
    S->>S: Check idempotency key
    S->>Repo: findByIdWithMenu(restaurantId)
    Repo-->>S: Restaurant with menu items
    S->>F: create(customerId, restaurant, items, address)
    F->>F: Read menu prices, compute total
    F->>F: Create Order with OrderItems
    F-->>S: ResultT<Order>
    S->>Repo: save(order)
    Repo->>DB: INSERT order, order_items, idempotency_key
    DB-->>Repo: committed
    S->>S: dispatch(domainEvents)
    S-->>R: ResultT<Order>
    R-->>C: 201 Created { orderId, totalAmount }
```

**Key points:**
- Server-side pricing via `OrderFactory.create()`
- Idempotency key checked in same transaction as the order
- Domain events dispatched after commit (ADR-013)
- `xmin` on `orders` enables optimistic concurrency (ADR-012)
