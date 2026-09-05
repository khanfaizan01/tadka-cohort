# Setup Guide

## Requirements

- **Java 21** (JDK 21 or later)
- **Maven 3.9** or later
- **Docker Desktop** (with Docker Compose v2)

## Installation Steps

### 1. Install Java 21

Verify installation:

```bash
java -version
```

Expected output: `openjdk version "21"` or similar.

If you need to install Java 21, use your preferred package manager or download from [adoptium.net](https://adoptium.net/).

### 2. Install Maven 3.9+

Verify installation:

```bash
mvn -version
```

Expected output includes: `Apache Maven 3.9.x`.

### 3. Install Docker Desktop

- Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Start Docker Desktop and ensure it is running
- Verify Docker Compose:

```bash
docker compose version
```

## Running the Project

1. **Compile the project**
   ```bash
   mvn compile
   ```

2. **Start infrastructure**
   ```bash
   docker compose up -d
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Check health**
   ```bash
   curl http://localhost:5224/health
   ```

## Stopping the Project

```bash
docker compose down -v
```

The `-v` flag removes the PostgreSQL volume, giving you a clean database on the next start.
