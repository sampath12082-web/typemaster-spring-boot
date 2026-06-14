# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This is the **Spring Boot backend** for TypeMaster, a touch-typing tutor web app. The React + Vite frontend lives in a sibling directory (`../frontend`) and is **not** tracked in this repo.

- Backend API: `http://localhost:8080`
- Frontend dev server: `http://localhost:5173` (proxies `/api/*` to the backend)
- Database: PostgreSQL (local default: `jdbc:postgresql://localhost:5432/typingtutor`)

## Commands

### Run the backend

```bash
# Requires JWT_SECRET environment variable, OR use the local profile (see below)
./mvnw spring-boot:run                                    # Linux/Mac
mvnw.cmd spring-boot:run                                  # Windows

# Local profile reads src/main/resources/application-local.properties (gitignored)
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Set up local properties by copying the example:
```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# Fill in DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET (32+ chars)
# and optionally MAIL_* and AI_API_KEY
```

Create the local PostgreSQL database before first run:
```sql
CREATE DATABASE typingtutor;
```

### Tests

```bash
mvnw.cmd test                                             # all tests
mvnw.cmd test -Dtest=ExamServiceTest                      # single test class
mvnw.cmd test -Dtest=ExamServiceTest#submitExam_pass      # single test method
```

Tests use Mockito (`@ExtendWith(MockitoExtension.class)`). The Surefire plugin adds `--add-opens` flags required for Mockito inline mocking on Java 21.

### Build

```bash
mvnw.cmd package -DskipTests                              # produces target/typing-tutor-backend-1.0.0.jar
```

### Scripts (Windows convenience)

```
scripts/start-all.bat       # launches backend + frontend in separate windows
scripts/start-backend.bat   # backend only (uses C:\Apache\maven\bin\mvn.cmd)
scripts/stop-all.bat        # kills both processes
```

## Architecture

### Package structure (`src/main/java/com/typingtutor/`)

| Package | Purpose |
|---------|---------|
| `controller/` | REST controllers — thin, delegate all logic to services |
| `service/` | Business logic (auth, lessons, placement, exams, certificates, AI gen) |
| `entity/` | JPA entities mapping to H2/PostgreSQL tables |
| `repository/` | Spring Data JPA interfaces |
| `dto/` | Request/response shapes; never expose entities directly |
| `security/` | JWT filter, `UserPrincipal`, `UserDetailsServiceImpl` |
| `config/` | `SecurityConfig` (filter chain, CORS), `SpaController` (SPA fallback) |

### Request pipeline

```
HTTP → JwtAuthFilter (extract + validate JWT, set SecurityContext)
     → SecurityFilterChain (public vs protected path rules)
     → @RestController
     → @Service (throws IllegalArgumentException / NoSuchElementException)
     → GlobalExceptionHandler (maps all exceptions → { "error": "..." } JSON)
```

All error responses follow `{ "error": "message" }`. HTTP status is set by `GlobalExceptionHandler`.

### Database

PostgreSQL everywhere — local dev and production both use it. Connection reads from env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (local defaults: `localhost:5432/typingtutor`, user `postgres`).

`data-prod.sql` runs on every startup (guarded by `WHERE NOT EXISTS`) to seed 24 lessons (8 per tier), 3 exams, and the default admin user. Hibernate `ddl-auto=update` creates/migrates tables automatically.

**Schema tables:** `app_users`, `lessons`, `user_performance`, `email_verification`, `exams`, `exam_attempts`, `certificates`, `inquiries`, `audit_log`

> `user_password` is named that way (not `password`) to avoid an H2 reserved word conflict.

**Delete cascade order for user deletion:** `certificates` → `exam_attempts` → `user_performance` → `inquiries` → `app_users`

### Authentication

- Stateless JWT (HS256, 24 h expiry). Secret from `${JWT_SECRET}` env var.
- `JwtAuthFilter` runs before `UsernamePasswordAuthenticationFilter`.
- Change-password tokens carry a `"purpose": "CHANGE_PASSWORD"` claim and expire in 15 minutes.
- No server-side token blacklist; logout is client-side only.
- Admin endpoints (`/api/admin/**`) require `ROLE_ADMIN` at both the filter chain and `@PreAuthorize`.

### Lesson progression model

Lessons unlock sequentially within a tier. Status per user is computed on the fly by `LessonService`:

- `LOCKED` — previous lesson not yet passed
- `AVAILABLE` — ready to attempt
- `PASSED` — met `min_wpm` AND `min_accuracy` in at least one attempt
- `FAILED_ATTEMPT` — attempted but not yet passing

Passing all 8 lessons in a tier unlocks that tier's certification **exam**. Failing an exam resets all lesson progress for that tier.

### Key services

- **`LessonService`** — computes per-user lesson statuses by querying all performance records
- **`ExamService`** — validates pass/fail against thresholds; on fail, deletes tier's `user_performance` rows; on pass, calls `CertificateService`
- **`CertificateService`** — generates PDF via PDFBox, stores binary in `certificates.pdf_data`
- **`OtpService`** — creates 6-digit OTPs with a 15-minute expiry; purpose enum: `VERIFY_EMAIL | FIRST_LOGIN | RESET_PASSWORD`
- **`LessonGenerationService`** — calls Anthropic API (Claude) if `AI_API_KEY` is set; silently disabled otherwise
- **`AuditLogService`** — logs admin actions to `audit_log` table

### Environment variables

| Variable | Required | Purpose |
|----------|----------|---------|
| `JWT_SECRET` | Yes | JWT signing secret (32+ chars) |
| `MAIL_HOST` / `MAIL_USERNAME` / `MAIL_PASSWORD` | No | SMTP; defaults to Gmail config |
| `AI_API_KEY` | No | Anthropic API key for AI lesson generation |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | No | Overrides default `http://localhost:*` |

## Important conventions

- Controllers are thin — no business logic, just parameter extraction and service delegation.
- Services throw `IllegalArgumentException` (→ 400) or `NoSuchElementException` (→ 404); `GlobalExceptionHandler` maps these.
- Placement test thresholds: `< 20 WPM → BASIC`, `20–39 → INTERMEDIATE`, `≥ 40 → ADVANCED`.
- WPM formula: `(charsTyped / 5) / elapsedMinutes`. Accuracy: `(correctKeys / totalKeys) * 100`.
- The H2 console is gated by `spring.h2.console.enabled`; frame-options are only disabled when that flag is true.
