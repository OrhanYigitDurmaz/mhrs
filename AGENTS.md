# AGENTS.md

## Project Snapshot
- Project: `mhrs-backend` (Spring Boot monolith)
- Language: Java
- Build tool: Maven (`./mvnw`)
- Current API list source of truth: `API_DOC.md`

## Stack
- Spring Boot `4.0.3`
- Java target: `21` (project compiles in current environment with newer JDK too)
- Spring Web MVC, Security, Validation, JPA, Flyway, Actuator
- PostgreSQL + Redis for local development (`docker-compose.yml`)
- Testcontainers for integration tests

## Required Architecture (Do Not Bypass)
Each feature module must follow:
1. `presentation` layer: API controllers + request/response DTOs
2. `application` layer: use cases/services + command/query models + ports
3. `domain` layer: entities/value objects/business rules
4. `infrastructure` layer: DB/external provider adapters implementing ports

In code, this maps to package structure:
- `com.mhrs.<module>.api`
- `com.mhrs.<module>.application`
- `com.mhrs.<module>.domain`
- `com.mhrs.<module>.infrastructure`

## Current Status
- `auth` module is scaffolded with the 4-layer pattern.
- `AuthController` endpoints exist and are wired through:
  - `AuthUseCase` (application)
  - domain models and password policy
  - `StubAuthGateway` (infrastructure adapter)
- Global exception handler maps:
  - `IllegalArgumentException` -> HTTP `400`
  - generic `Exception` -> HTTP `500`

## Module Scaffolding Rules
- Keep controllers thin: map DTOs to application commands.
- Business rules belong in `domain` (not controllers, not infra).
- Application services orchestrate use cases and depend on `port` interfaces only.
- Infrastructure implements ports and hides framework/external details.
- Prefer immutable request/response types (`record`).
- Keep endpoint paths exactly aligned with `API_DOC.md`.

## Suggested Next Modules Order
1. `doctor`
2. `patient`
3. `appointment`
4. `waitlist`
5. `notification`
6. `admin`

For each module:
1. Create `api/application/domain/infrastructure` packages.
2. Add controller + DTOs for endpoints from `API_DOC.md`.
3. Add use case interfaces + commands/queries.
4. Add domain models and explicit business rule classes.
5. Add infrastructure stub adapter implementing application ports.

## Testing Expectations
Current tests only verify application context startup.

For each module, add:
1. `@WebMvcTest` for controller contract and validation.
2. Unit tests for domain rules and application service orchestration.
3. At least one integration test path where useful.

Run tests with:
- `cd mhrs-backend`
- `./mvnw test`

## Local Run
1. Start infra: `docker compose up -d` (from `mhrs-backend/`)
2. Run app: `./mvnw spring-boot:run`
3. Default profile is `local`

## Profiles and Config
- Base config: `src/main/resources/application.yaml`
- Profiles:
  - `application-local.yaml`
  - `application-test.yaml`
  - `application-prod.yaml`

## Guardrails
- Do not collapse architecture back into controller->repository direct calls.
- Do not put domain rules in DTO annotations only.
- Do not introduce endpoint paths not listed in `API_DOC.md` unless explicitly requested.
- Keep changes incremental and buildable after each module.
