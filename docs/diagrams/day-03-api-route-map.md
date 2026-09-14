# Day 3 — API Route Map

All REST endpoints introduced on Day 3.

```mermaid
graph LR
    subgraph GET["GET"]
        G1["/api/v1/restaurants"]
        G2["/api/v1/restaurants/{id}/menu"]
        G3["/api/v1/orders/{id}"]
        G4["/api/v1/health"]
        G5["/api/v1/health/ready"]
    end

    subgraph POST["POST"]
        P1["/api/v1/orders"]
    end

    subgraph PATCH["PATCH"]
        Pa1["/api/v1/restaurants/{id}/menu/{menuId}"]
        Pa2["/api/v1/restaurants/{id}"]
        Pa3["/api/v1/orders/{id}/status"]
    end

    subgraph Health["Health"]
        H1["/health"]
        H2["/health/ready"]
    end
```

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/restaurants` | List all restaurants |
| GET | `/api/v1/restaurants/{id}/menu` | Get restaurant menu |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| POST | `/api/v1/orders` | Place a new order |
| PATCH | `/api/v1/restaurants/{id}/menu/{menuId}` | Update menu item price |
| PATCH | `/api/v1/restaurants/{id}` | Update restaurant (deactivate) |
| PATCH | `/api/v1/orders/{id}/status` | Transition order status |
