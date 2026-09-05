# Docker Compose Cheat Sheet

## Core Commands

```bash
# Start all services
docker compose up -d

# Stop and remove containers, networks, and volumes
docker compose down -v

# View running containers
docker compose ps

# View logs
docker compose logs -f postgres

# Run a one-off command on a service
docker exec tadka-postgres psql -U tadka -d tadka -c "\dn"
```

## Override Files

- `docker-compose.yml` — base configuration (PostgreSQL 16, healthcheck, placeholders)
- `docker-compose.override.yml` — development-only overrides (host port mapping, SQL logging)

Override files are automatically loaded by `docker compose` and are never deployed to production.

## Volume Management

```bash
# List volumes
docker volume ls

# Remove a specific volume
docker volume rm tadka-cohort_pgdata
```

## Useful Patterns

```bash
# Inspect a container's environment
docker exec tadka-postgres env | grep POSTGRES_

# Connect to PostgreSQL interactively
docker exec -it tadka-postgres psql -U tadka -d tadka

# Run a PostgreSQL query from the host
docker exec tadka-postgres psql -U tadka -d tadka -c "SELECT 1;"
```
