# AGENTS.md

## Purpose
This file helps AI coding agents work productively in the TypeMaster backend repository.

## What this repo is
- Spring Boot backend for TypeMaster.
- Uses Maven with bundled wrappers (`mvnw`, `mvnw.cmd`).
- Main code lives under `src/main/java/com/typingtutor`.
- Tests live under `src/test/java`.
- Configuration is in `src/main/resources`.

## Recommended commands
- Run the app:
  - Windows: `mvnw.cmd spring-boot:run`
  - Linux/Mac: `./mvnw spring-boot:run`
- Run all tests:
  - Windows: `mvnw.cmd test`
  - Linux/Mac: `./mvnw test`
- Build package:
  - Windows: `mvnw.cmd package -DskipTests`
  - Linux/Mac: `./mvnw package -DskipTests`

## Key areas
- `src/main/java/com/typingtutor/controller` — REST controllers.
- `src/main/java/com/typingtutor/service` — business logic.
- `src/main/java/com/typingtutor/entity` — JPA entities.
- `src/main/java/com/typingtutor/repository` — Spring Data repositories.
- `src/main/java/com/typingtutor/security` — JWT filter, user details, auth.
- `src/main/java/com/typingtutor/config` — security and application configuration.
- `src/main/java/com/typingtutor/dto` — request/response DTOs.

## Conventions
- Keep controllers thin; delegate logic to services.
- Do not expose JPA entities directly in API responses.
- Use `@Valid` DTOs for request payload validation.
- JWT secrets and environment-specific settings are loaded from env vars.
- Local dev config is in `application-local.properties` and should be copied from the example.

## Important docs
- `README.md` — repo overview and quick start.
- `CLAUDE.md` — backend architecture, commands, and security audit notes.
- `docs/quality/SECURITY_AUDIT.md` — active security findings and recommended fixes.

## Special notes for agents
- Prefer using the bundled Maven wrapper rather than a system Maven install.
- Preserve existing security behavior and JWT configuration patterns.
- When changing tests, run `mvnw.cmd test` to verify both main code and test code compile.
- If a change touches authentication, authorization, or JWT handling, search for `JwtAuthFilter`, `SecurityConfig`, and `UserDetailsServiceImpl`.

## Suggested next agent customizations
- Add a `backend-testing` skill that knows the repo's Maven wrapper commands and common test patterns.
- Add a `security-audit` instruction file that links to `docs/security/SECURITY_AUDIT.md` and enforces the repository's current security findings.
