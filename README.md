# Tadka

Java food delivery platform (Day 2).

## Stack

- **Java 21**
- **Spring Boot 3.5.16**
- **PostgreSQL 16**
- **Spring Data JPA**
- **Flyway**

## Quick Start

```bash
mvn compile
docker compose up -d
mvn spring-boot:run
curl http://localhost:5224/health
```

## Project Structure

```
tadka-cohort/
├── docker-compose.yml           # Infrastructure (PostgreSQL 16 + placeholders)
├── docker-compose.override.yml  # Development overrides
├── docs/
│   ├── adrs/                    # Architecture Decision Records
│   ├── runbooks/                # Daily runbooks
│   ├── learn/                   # Learning guides
│   ├── templates/               # ADR template
│   └── diagrams/                # Architecture diagrams
└── src/                         # Application source code
```

## Documentation

- **ADRs:** See [`docs/adrs/`](docs/adrs/)
- **Runbooks:** See [`docs/runbooks/`](docs/runbooks/)
- **Learning:** See [`docs/learn/`](docs/learn/)
