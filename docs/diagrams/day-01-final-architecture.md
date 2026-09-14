# Day 1 — Final Architecture

The target architecture for the Tadka platform: one Spring Boot API, one PostgreSQL 16 database, five schemas.

```mermaid
graph TB
    subgraph Client["Clients"]
        Mobile["Mobile App"]
        Web["Web App"]
    end

    subgraph API["Tadka API"]
        Health["HealthController<br/>GET /health, /health/ready"]
        Restaurant["RestaurantsController<br/>/api/v1/restaurants"]
        Orders["OrdersController<br/>/api/v1/orders"]
    end

    subgraph DB["PostgreSQL 16"]
        subgraph Identity["identity schema"]
            Users["users"]
            Addresses["user_addresses"]
        end
        subgraph Restaurant["restaurant schema"]
            Rest["restaurants"]
            Menu["menu_items"]
        end
        subgraph Ordering["ordering schema"]
            Ord["orders"]
            Items["order_items"]
        end
        subgraph Delivery["delivery schema"]
            Agents["delivery_agents"]
            Assign["delivery_assignments"]
        end
        subgraph Payment["payment schema"]
            Pay["payments"]
        end
    end

    Client -->|HTTP| API
    API -->|JPA/Hibernate| DB
```

**Key properties:**
- Single deployable (`docker compose up`)
- Five schemas for five bounded contexts
- No cross-schema FKs (ADR-008)
- Value objects (`Money`, `Address`) stored as columns, not separate tables
- `/health` = liveness, `/health/ready` = readiness with DB check
