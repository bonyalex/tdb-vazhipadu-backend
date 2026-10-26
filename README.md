# tdb-vazhipadu-backend

Vazhipadu (temple offering) booking service for a temple board.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Data JPA with H2 (in-memory)
- Maven (wrapper included)

## Run the app

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080` (override with `PORT`). Health check:
`http://localhost:8080/actuator/health`.

## Run the tests

```bash
./mvnw test
```

## Pull requests

| Problem | PR |
|---|---|
| P1 - Offerings and Orders API | _pending_ |
| P2 - Order Search | _pending_ |
| P3 - Daily Limits | _pending_ |
| P4 - Cancellation and Daily Collection | _pending_ |
