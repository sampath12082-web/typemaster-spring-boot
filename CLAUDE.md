# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repository is

This is the **Spring Boot backend** for TypeMaster, a touch-typing tutor web app. The React + Vite frontend lives in a sibling directory (`../typemaster-ui`) and is **not** tracked in this repo.

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

**Test inventory:**

| File | Type | What it covers |
|------|------|----------------|
| `service/UserServiceLoginTest` | Unit | Login flows: no-email, unverified, first-login OTP |
| `service/UserServiceUpdatePasswordTest` | Unit | `PUT /api/auth/me/password` service logic — correct/wrong current password, same-password guard, audit log |
| `service/OtpServiceTest` | Unit | OTP generation, expiry, attempt locking |
| `service/OtpServiceIntegrationTest` | Integration (`@SpringBootTest`) | OTP attempt lockout against real DB |
| `dto/RegisterRequestValidationTest` | DTO validation | Age constraint on date-of-birth |
| `dto/UpdatePasswordRequestValidationTest` | DTO validation | `currentPassword` blank, `newPassword` pattern (all missing-char combinations) |
| `security/JwtAuthFilterTest` | Unit | Invalid token → 401 |
| `security/JwtAuthFilterEmailTest` | Unit | Email verification gate |
| `security/JwtStartupValidatorTest` | Integration | JWT secret present at startup (run by CI smoke) |
| `service/ExamServiceTest` | Unit | Tier-not-complete guard, pass → certificate issuance, fail → no certificate, invalid tier |
| `service/LessonServiceTest` | Unit | Per-user lesson unlock/pass/fail status computation |
| `service/PlacementServiceTest` | Unit | WPM → tier thresholds (`<20` Basic, `20-39` Intermediate, `≥40` Advanced), passage/time-limit retrieval |
| `service/EmailServiceTest` | Unit | Email sending logic |
| `security/PasswordCryptoServiceTest` | Unit | RSA-OAEP encrypt/decrypt round-trip, public key export |
| `security/PasswordPolicyTest` | Unit | Password complexity regex — all missing-char combinations |

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
| `entity/` | JPA entities mapping to PostgreSQL tables |
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
- `GET /api/auth/leaderboard` — public endpoint returning top users ranked by WPM.

### Password transport encryption

`PasswordCryptoService` encrypts login/password-change payloads with RSA-OAEP (SHA-256) so passwords never travel as plaintext in the request body (defense-in-depth on top of TLS). The RSA keypair is generated fresh on each server start and kept in-memory only. The frontend fetches the public key via `GET /api/auth/public-key` immediately before every encrypt call, so a restart never leaves clients holding a stale key. Password fields in DTOs now carry RSA ciphertext — `PasswordPolicy` validates the decrypted plaintext server-side.

### Password policy

`PasswordPolicy` centralises the password complexity rule: 16–20 characters, at least one uppercase, one lowercase, one digit, one special character (`@$!%*?&`). Previously a `@Pattern` on DTO fields, now checked against decrypted plaintext in service code.

**Password change flows:**

| Flow | Endpoint | Auth | When used |
|------|----------|------|-----------|
| OTP-based (forgot / first-login) | `POST /api/auth/change-password` | Public (protected by short-lived `changePasswordToken` in body) | Forgot password, first-login forced change |
| Authenticated change | `PUT /api/auth/me/password` | JWT required | Logged-in user voluntarily changing their password |

`PUT /api/auth/me/password` body: `{ currentPassword, newPassword }`. Validates current password via BCrypt before accepting the new one; rejects if new == current. Same password regex as registration.

### Dark mode

Dark mode is fully implemented via ThemeContext with localStorage persistence (key: tt_theme) and system prefers-color-scheme detection. Toggle in Navbar. All pages and most components include dark: Tailwind variants.

### Frontend pages

| Page | Route | Description |
|------|-------|-------------|
| `LandingPage` | `/` (unauthenticated) | Public marketing page with hero, features, CTA |
| `LoginPage` | `/login` | Username/password login |
| `RegisterPage` | `/register` | New user registration with email |
| `VerifyEmailPage` | `/verify-email` | OTP email verification |
| `ChangePasswordPage` | `/change-password` | OTP-based password change (forgot/first-login) |
| `PlacementPage` | `/placement` | 60-second placement test or skip |
| `DashboardPage` | `/dashboard` | Main hub — tier sections, lesson cards, stats, quick resume |
| `LessonPage` | `/lesson/:id` | Typing engine with real-time WPM/accuracy |
| `ExamPage` | `/exam/:tier` | Timed certification exam |
| `AnalyticsPage` | `/analytics` | Progress charts, rankings, activity heatmap |
| `LeaderboardPage` | `/leaderboard` | Global rankings by best WPM |
| `CertificatesPage` | `/certificates` | Earned certificates with PDF download |
| `CertificateVerifyPage` | `/verify/:certId` | Public certificate verification |
| `HelpPage` | `/help` | AI assistant, support tickets, FAQ |
| `ProfilePage` | `/profile` | User info, email, change password |
| `AboutPage` | `/about` | App mission and features |
| `AdminPage` | `/admin` | User/inquiry management, audit logs |

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
- **`HelpAgentService`** — separate Anthropic-backed support chatbot; calls `https://api.anthropic.com/v1/messages` directly via `java.net.http.HttpClient` with a fixed product-knowledge system prompt, returns `{ answer, escalate }`
- **`AuditLogService`** — logs admin actions to `audit_log` table
- **`PlacementService`** — serves the placement passage/time limit and maps WPM to starting tier (`< 20 BASIC`, `20-39 INTERMEDIATE`, `≥ 40 ADVANCED`)
- **`AdminService`** — admin user CRUD/reset-password; deletes a user's `inquiries`/`certificates`/etc. in FK-safe order (see cascade order above)
- **`InquiryService`** — support inquiry lifecycle (create, resolve, reopen) backing `InquiryController`
- **`EmailService`** — sends verification/OTP/notification emails via `spring-boot-starter-mail`
- **`UserService`** — auth, registration, login, profile updates, stats, rankings, leaderboard
- **`PerformanceService`** — saves lesson attempt results, retrieves user history

### Environment variables

| Variable | Required | Purpose |
|----------|----------|---------|
| `JWT_SECRET` | Yes | JWT signing secret (32+ chars) |
| `DB_URL` | No | JDBC connection URL (default: `jdbc:postgresql://localhost:5432/typingtutor`) |
| `DB_USERNAME` | No | Database username (default: `postgres`) |
| `DB_PASSWORD` | No | Database password (default: `postgres`) |
| `MAIL_HOST` / `MAIL_USERNAME` / `MAIL_PASSWORD` | No | SMTP; defaults to Gmail config |
| `AI_API_KEY` | No | Anthropic API key for AI lesson generation |
| `CORS_ALLOWED_ORIGINS` | No | Overrides default `http://localhost:*` |

### Deployment

`.github/workflows/deploy.yml` runs on every push to `main`: builds with `mvn package -DskipTests -B`, then POSTs to `${{ secrets.RENDER_DEPLOY_HOOK_URL }}` to trigger a Render deploy — there is no test gate before deploy. `Dockerfile` is a two-stage build (`maven:3.9.6-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine`), exposes port 8080.

## Important conventions

- Controllers are thin — no business logic, just parameter extraction and service delegation.
- Services throw `IllegalArgumentException` (→ 400) or `NoSuchElementException` (→ 404); `GlobalExceptionHandler` maps these.
- Any method that writes to the DB must be `@Transactional`. Read-only methods benefit from `@Transactional(readOnly = true)`.
- Logging: SLF4J only (`LoggerFactory.getLogger`), never `System.out.println`. Sensitive data (passwords, OTPs) must never appear in INFO-level logs.
- Timestamps: use `@CreationTimestamp`, not `LocalDateTime.now()` in field initialisers.
- `app.dev-otp-enabled=true` (set only in `application-local.properties`) returns OTPs in API responses for local development. Never enable in production.
- Commit message format: `<type>: <short imperative summary>` — types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`.
- Placement test thresholds: `< 20 WPM → BASIC`, `20–39 → INTERMEDIATE`, `≥ 40 → ADVANCED`.
- WPM formula: `(charsTyped / 5) / elapsedMinutes`. Accuracy: `(correctKeys / totalKeys) * 100`.
- The H2 console is gated by `spring.h2.console.enabled`; frame-options are only disabled when that flag is true.

## Further reading

- `docs/architecture/HLD.md` / `LLD.md` — high/low-level design
- `docs/quality/SECURITY_AUDIT.md` — tracked findings; check status before assuming a listed issue is still open, some predate recent commits
- `docs/quality/TEST_PLAN.md`, `TESTING.md`, `BUGS.md`, `CODE_REVIEW.md`, `ENHANCEMENTS.md` — test strategy, known bugs, review notes
- `docs/standards/CODING_STANDARDS.md` — naming, layering, transaction, and logging conventions for both backend and frontend
