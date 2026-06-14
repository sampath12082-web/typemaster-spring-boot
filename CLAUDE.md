# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

The Maven wrapper (`mvnw`) is broken on this machine. Use the global `mvn` binary:

```powershell
# First-time setup
Copy-Item src\main\resources\application-local.properties.example `
          src\main\resources\application-local.properties
# Edit application-local.properties to contain: app.jwt.secret=<32+ char Base64 secret>
# Do NOT write JWT_SECRET=... — that env-var form does not override the ${JWT_SECRET} placeholder

# Run (Windows — use the Maven plugin parameter, not -Dspring.profiles.active)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Build JAR
mvn clean package

# All tests
mvn test

# Single test class
mvn test -Dtest=OtpServiceTest
```

**Optional env vars** (set in `application-local.properties` or as environment variables):
- `AI_API_KEY` — enables `LessonGenerationService` (AI lessons) and `HelpAgentService` (AI chat); both degrade gracefully without it
- `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` — when `MAIL_USERNAME` is blank, `EmailService.isMailEnabled()` returns `false` and OTP codes are echoed in the response body as `devOtp`

## Package structure

`com.typingtutor.{config, controller, dto, entity, repository, security, service}`

- `config/` — `SecurityConfig` (CORS, filter chain, session policy)
- `controller/` — one `@RestController` per domain + `GlobalExceptionHandler`
- `security/` — `JwtAuthFilter`, `JwtUtil`, `UserDetailsServiceImpl`, `UserPrincipal`
- `service/` — business logic; throws `IllegalArgumentException` or `NoSuchElementException` for domain errors
- `repository/` — Spring Data JPA interfaces
- `entity/` / `dto/` — JPA entities and request/response DTOs

## Key backend details

### JWT implementation

**Correction vs root CLAUDE.md**: tokens are NOT distinguished by an `aud` claim. `JwtUtil` uses a `purpose` claim:
- **Auth token** — no `purpose` claim; 24h expiry (`app.jwt.expiration-ms`)
- **Change-password token** — `purpose=CHANGE_PASSWORD`; 15-minute expiry; validated by `JwtUtil.extractChangePasswordUsername()` which returns `null` if the claim is absent or the token is expired

The `JwtAuthFilter` passes change-password tokens through — they carry a valid signature and subject. The `/api/auth/change-password` endpoint consumes them via `extractChangePasswordUsername()`; all other secured endpoints just call `extractUsername()` and do not check the `purpose` claim.

### Public vs protected endpoints

**Correction vs root CLAUDE.md**: NOT all of `/api/auth/**` is public. `SecurityConfig` permits only these specific paths:

```
/api/auth/login
/api/auth/register
/api/auth/verify-email
/api/auth/resend-otp
/api/auth/verify-otp
/api/auth/change-password
/api/auth/forgot-password
/api/certificates/*
```

`GET /api/auth/me`, `PUT /api/auth/me`, and `GET /api/auth/ranking` all require a valid JWT.

### Exception → HTTP status mapping (GlobalExceptionHandler)

| Exception | HTTP |
|-----------|------|
| `EmailNotVerifiedException` (inner class of `UserService`) | 403 |
| `LessonLockedException` (inner class of `PerformanceService`) | 403 |
| `IllegalArgumentException` | 400 |
| `NoSuchElementException` | 404 |
| `BadCredentialsException` | 401 |
| `DisabledException` / `LockedException` | 401, body: `ACCOUNT_INACTIVE` |
| `AccessDeniedException` | 403 |

### OTP details

- 6-digit code, 30-minute expiry, 5-attempt lockout per verification record
- Stored in plaintext; comparison uses `MessageDigest.isEqual` (constant-time)
- `createOtp()` creates a new record every call — multiple valid records can exist; only the latest is used (`findUnusedByEmail` returns all, but `findValidOtp` takes `results.get(0)`)

### AI features

Two services use the Anthropic API (configured by `app.ai.api-key`, `app.ai.api-url`, `app.ai.model`):
- `LessonGenerationService.generateAdvancedLessonForUser()` — requires all 8 standard ADVANCED lessons passed; creates an `ai_generated=true` lesson persisted to the DB
- `HelpAgentService.chat()` — responds with `{"answer": "...", "escalate": true|false}`

Both return placeholder/fallback responses when the API key is absent.

### Tests

6 test classes in `src/test/`, all unit tests with Mockito (`@ExtendWith(MockitoExtension.class)`). No Spring context is loaded — fast to run individually.

### H2 console (dev only)

URL: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:file:./data/typingtutor`  
Username: `sa` / Password: *(empty)*
