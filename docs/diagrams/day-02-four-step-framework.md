# Day 2 — Four-Step Framework

The four-step framework for building each domain: define the schema, create the entity, add the repository, write the tests.

```mermaid
graph TD
    A[1. Create Flyway migration] --> B[2. Define @Entity class]
    B --> C[3. Create Spring Data JPA Repository]
    C --> D[4. Write integration tests]
    
    subgraph Step1["Step 1: Migration"]
        A -->|V__*.sql| Flyway["CREATE TABLE ..."]
    end
    subgraph Step2["Step 2: Entity"]
        B -->|@Entity @Table| JPA["@Column, @Embeddable, @Version"]
    end
    subgraph Step3["Step 3: Repository"]
        C -->|JpaRepository| CRUD["findById, findByX, save"]
    end
    subgraph Step4["Step 4: Tests"]
        D -->|@SpringBootTest| Assert["assertThat(result).isNotNull"]
    end
```

**Rules:**
- Each domain gets its own schema
- Each entity gets its own file
- Each repository extends `JpaRepository<Entity, UUID>`
- Tests use Testcontainers for PostgreSQL
