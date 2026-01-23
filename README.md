# Crypto Recommendation Service

## Overview

This project implements a **Crypto Recommendation Service** that ingests historical cryptocurrency price data from CSV files, persists it to a database on startup, and exposes REST APIs for querying statistics and recommendations based on the available data.

The application is built using **Spring Boot**, follows a **layered architecture**, and is designed with **extensibility, performance, and testability** in mind.

## Features & Requirements Coverage

### Implemented Functional Requirements

- Reads all crypto prices from CSV files on application startup
- Calculates **oldest, newest, minimum, and maximum** prices for each crypto
- Exposes an endpoint returning **cryptos sorted by normalized range**
- Exposes an endpoint returning **stats for a requested crypto**
- Exposes an endpoint returning the **crypto with the highest normalized range for a given day**

### Normalized Range

The normalized range is calculated as:

```
(max_price - min_price) / min_price
```

This allows fair comparison between cryptos with different absolute price scales.

### Rate Limiting

The API is protected by a simple IP-based rate limiting filter implemented using Bucket4j.

- Limit: **60 requests per minute per client IP**
- Purpose: protect the service from accidental or abusive traffic
- Scope: applies to all API endpoints

This solution is intentionally simple and in-memory, suitable for a single-instance setup.
In a production environment, a distributed rate limiter (e.g. Redis-backed) would be preferred.

## API Endpoints

All endpoints are documented using **OpenAPI / Swagger UI**.

After starting the application, open:

```
http://localhost:8080/swagger-ui.html
```

### Get crypto statistics

```
GET /api/cryptos/{symbol}/stats
```

**Query parameters (optional):**

* `from` – start date (YYYY-MM-DD)
* `to` – end date (YYYY-MM-DD)

**Example:**

```
GET /api/cryptos/BTC/stats?from=2023-01-01&to=2023-01-31
```

Returns:

* oldest price
* newest price
* minimum price
* maximum price

### Get cryptos sorted by normalized range

```
GET /api/cryptos/normalized-range
```

Returns all supported cryptos sorted **descending** by normalized range.

### Get crypto with highest normalized range for a day

```
GET /api/cryptos/highest-normalized-range?date=2023-01-01
```

Returns the crypto that had the highest normalized range on the given day.

## Error Handling

The application uses **custom runtime exceptions** and a centralized `@RestControllerAdvice`:

| Scenario                  | HTTP Status     |
| ------------------------- | --------------- |
| Unsupported crypto symbol | 404 Not Found   |
| No data available         | 404 Not Found   |
| Invalid date range        | 400 Bad Request |

All error responses follow a **standardized JSON structure**.

## Data Ingestion Strategy

* CSV files are loaded **once on application startup**
* Data is persisted into a relational database (H2 for local/testing)
* The crypto symbol is derived from the CSV filename
* No CSV parsing occurs during request handling

### CSV Format

```csv
timestamp,symbol,price
1641009600000,BTC,46813.21
1641020400000,BTC,46979.61
1641031200000,BTC,47143.98
1641034800000,BTC,46871.09
1641045600000,BTC,47023.24
```

---

### ⚠️ Note on Production Ingestion

For simplicity, this implementation assumes an empty database on startup (e.g. in-memory H2).

In a real production setup with a persistent database, CSV ingestion would typically be handled via:

* Flyway / Liquibase migrations
* or a versioned ingestion history table


## Scalability & Extensibility

### Adding New Cryptos

- Simply adding a new CSV file is enough
- No code changes required

### Supporting Longer Time Frames

- All endpoints support **date range parameters**
- Queries work for 1 month, 6 months, or longer periods

### Unsupported Cryptos

- Safely handled with explicit errors

## Architecture Overview

The project follows a **layered architecture**:

```
controller → service → repository → database
```

Additional layers:

* `ingestion` – CSV ingestion and parsing
* `exception` – custom exceptions and global handling
* `filter` – ip rate limiting

The design follows **SOLID principles**, with clear separation of concerns.

## Testing Strategy

- **Unit tests** for business logic (services)
- **Integration tests** for:
  * controllers
  * CSV ingestion
  * database interaction
  * rate limiting filter
- **No manual setup required**
- **JaCoCo coverage ≥ 80%** (DTOs/entities excluded intentionally)

To run tests:

```bash
mvn clean verify
```

## Performance Considerations

* CSVs are parsed **once** on startup
* All queries operate on the database
* Pagination is used internally to avoid loading unnecessary rows
* No full table scans for min/max calculations

## Running the Application

```bash
mvn spring-boot:run
```
Or via the docker image (see Docker Support section).

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## Docker Support

The application can be containerized using **two supported approaches**.

### Spring Boot Buildpacks

The preferred way to build a container image is using Spring Boot’s built-in
support for **Cloud Native Buildpacks**.

```bash
mvn spring-boot:build-image
```

This command produces an optimized, layered OCI image without requiring a
Dockerfile.

It can be run with:

```bash
docker run -p 8080:8080 crypto-recommendation:0.0.1-SNAPSHOT
```
Note: The image tag reflects the project version and may change if the version in
pom.xml is updated

This approach is recommended because it:

* follows Spring Boot’s official containerization strategy
* produces secure and minimal base images
* supports efficient layer caching
* reduces long-term maintenance overhead

---

### Dockerfile (Alternative)

For environments that require explicit control over the base image, JVM options,
or OS-level dependencies, the application can also be containerized using a
traditional Dockerfile.

```bash
docker build -t crypto-recommendation .
```

It can be run with:

```bash
docker run -p 8080:8080 crypto-recommendation
```

This approach may be preferred when:

* a custom base image is required
* additional OS-level packages must be installed
* fine-grained JVM tuning is necessary

## Future Improvements

If extended further, the following would be considered:

* Persistent database (PostgreSQL/MySQL)
* Versioned CSV ingestion (Flyway/Liquibase)
* Caching for hot queries
* Asynchronous ingestion for large datasets
* Observability (metrics, tracing)
