# Day 1 — Monolith Architecture

The Tadka platform starts as a monolith (ADR-002). One Spring Boot application, one database, one compose file.

```mermaid
graph LR
    subgraph App["Tadka API (Spring Boot)"]
        Controller["Controllers"]
        Service["Services"]
        Domain["Domain Models"]
        Repository["Repositories"]
        Config["Config"]
    end

    subgraph DB["PostgreSQL 16"]
        S1["identity"]
        S2["restaurant"]
        S3["ordering"]
        S4["delivery"]
        S5["payment"]
    end

    Controller --> Service
    Service --> Domain
    Domain --> Repository
    Repository --> DB
    Config --> Controller
    Config --> Service
```

**Why monolith first:**
- Single deployable — `docker compose up`
- No network calls between domains
- Transactional boundaries are straightforward
- Easy to refactor into services later

**Future extraction points:**
- `ordering` → order service
- `restaurant` → restaurant service
- `delivery` → delivery service
- `payment` → payment service
- `identity` → identity/auth service
