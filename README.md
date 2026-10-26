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

Set the admin login and the JWT signing key, then start the service. The values below are
throwaway examples for local use only; choose your own and never commit real ones.

Bash:

```bash
export ADMIN_EMAIL=admin@temple.example
export ADMIN_PASSWORD=change-me-admin-password
export JWT_SIGNING_KEY=local-dev-signing-key-at-least-32-characters-long
./mvnw spring-boot:run
```

PowerShell:

```powershell
$env:ADMIN_EMAIL = "admin@temple.example"
$env:ADMIN_PASSWORD = "change-me-admin-password"
$env:JWT_SIGNING_KEY = "local-dev-signing-key-at-least-32-characters-long"
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080` (override with `PORT`). Health check:
`http://localhost:8080/actuator/health` (needs the bearer token like every other endpoint).

### Log in and call the API

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@temple.example","password":"change-me-admin-password"}' \
  | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/offerings?templeId=1
```

Every response uses the envelope `{ "success": ..., "data": ..., "error": ... }`; the token is at
`data.accessToken` in the login response. A missing, invalid or expired token gives `401`.

In the Bruno collection, start Bruno from a shell that has `ADMIN_EMAIL` and `ADMIN_PASSWORD` set and
run `00 Login` first; the other requests reuse the token.

## Run the tests

```bash
./mvnw test
```

## Pull requests

| Problem | PR |
|---|---|
| P1 - Offerings and Orders API | [#7](https://github.com/bonyalex/tdb-vazhipadu-backend/pull/7) |
| P2 - Order Search | [#8](https://github.com/bonyalex/tdb-vazhipadu-backend/pull/8) |
| P3 - Daily Limits | [#9](https://github.com/bonyalex/tdb-vazhipadu-backend/pull/9) |
| P4 - Cancellation and Daily Collection | [#10](https://github.com/bonyalex/tdb-vazhipadu-backend/pull/10) |
| Response envelope | [#13](https://github.com/bonyalex/tdb-vazhipadu-backend/pull/13) |
| Admin login (JWT) | [#14](https://github.com/bonyalex/tdb-vazhipadu-backend/pull/14) |
