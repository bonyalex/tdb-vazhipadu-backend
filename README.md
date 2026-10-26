# tdb-vazhipadu-backend

Vazhipadu (temple offering) booking service for a temple board.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Data JPA with H2 (in-memory)
- Maven (wrapper included)

## Configuration

Every endpoint except `POST /api/auth/login` requires `Authorization: Bearer <token>`. The app
refuses to start unless these environment variables are set:

| Variable | Meaning |
|---|---|
| `ADMIN_EMAIL` | Admin login email |
| `ADMIN_PASSWORD` | Admin login password |
| `JWT_SIGNING_KEY` | Token signing key, at least 32 characters |
| `JWT_TTL` | Optional token lifetime as an ISO-8601 duration, default `PT1H` |

## Run the app

```bash
export ADMIN_EMAIL=<email> ADMIN_PASSWORD=<password> JWT_SIGNING_KEY=<32+ char key>
./mvnw spring-boot:run
```

Get a token with `POST /api/auth/login` and body `{"email": "...", "password": "..."}`. In the
Bruno collection, run `00 Login` first (it reads `ADMIN_EMAIL` and `ADMIN_PASSWORD` from the
environment Bruno was started in); the other requests reuse the token.

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
