# TypeMaster — High Level Design (HLD)

_Version: 1.0 | Last updated: 2026-06-12_

---

## 1. System Overview

TypeMaster is a full-stack typing tutor web application that helps users improve their typing speed and accuracy through structured lessons, placement testing, tier-based progression, and certification exams.

```
┌─────────────────────────────────────────────────────────┐
│                    Browser (User)                        │
│           React 18 SPA — Vite 5 — Tailwind CSS          │
│                   http://localhost:5173                   │
└──────────────────────┬──────────────────────────────────┘
                       │ REST (JSON over HTTP)
                       │ Bearer JWT in Authorization header
                       │ /api/* proxied by Vite in dev
┌──────────────────────▼──────────────────────────────────┐
│                  Backend API Server                       │
│         Spring Boot 3.2.5 — Java 21 — Maven              │
│                   http://localhost:8080                   │
└──────────────────────┬──────────────────────────────────┘
                       │ JPA / Hibernate
┌──────────────────────▼──────────────────────────────────┐
│                  PostgreSQL Database                      │
│      jdbc:postgresql://localhost:5432/typingtutor         │
│             Persists between server restarts             │
└─────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Frontend runtime | React | 18 | UI component framework |
| Frontend build | Vite | 5 | Dev server, HMR, production bundling |
| Frontend styling | Tailwind CSS | 3 | Utility-first CSS |
| Frontend routing | React Router | 6 | SPA client-side routing |
| Frontend HTTP | Axios | latest | API communication + interceptors |
| Backend framework | Spring Boot | 3.2.5 | REST API, DI container |
| Backend language | Java | 21 | Application logic |
| Backend build | Maven | 3.x | Dependency management, packaging |
| Security | Spring Security | 6 | JWT authentication, authorisation |
| JWT library | jjwt | 0.11+ | Token generation and validation |
| ORM | Hibernate / JPA | 6.x | Entity mapping, query execution |
| Database | PostgreSQL | 18 | Relational database (all environments) |
| Email | Spring Mail | 3.x | OTP delivery via SMTP |
| AI lessons | External HTTP API | — | AI-generated lesson content |
| E2E testing | Playwright | latest | Browser automation tests |

---

## 3. User Roles

| Role | Description | Capabilities |
|------|-------------|-------------|
| `GUEST` | Unauthenticated visitor | Access `/login`, `/register` only |
| `USER` | Registered and verified member | Lessons, placement, analytics, profile, help, exams, certificates |
| `ADMIN` | Administrator | All USER capabilities + `/admin` panel (user management, inquiry resolution) |

---

## 4. Authentication & Session Flow

```
Register ──► /verify-email (OTP) ──► Login ──► JWT issued
                                              │
                    ┌─────────────────────────┼──────────────────────┐
                    │                         │                       │
              New user?                 Has email?            No email?
                    │                         │                       │
         ► /placement test         emailVerified=false?    placementCompleted?
                    │                         │                       │
         ► Dashboard            ► /verify-email          ► Dashboard
```

**JWT Lifecycle:**
- Token stored in `localStorage` as `tt_token`
- 24-hour expiry (configurable via `JWT_EXPIRATION_MS`)
- Sent as `Authorization: Bearer <token>` on every API request
- Axios interceptor adds token automatically and redirects to `/login` on 401
- No server-side token blacklist (logout is client-side removal only)

---

## 5. Core Feature Modules

### 5.1 Lesson Progression
```
BASIC tier (8 lessons) ──► INTERMEDIATE tier (8 lessons) ──► ADVANCED tier (8 lessons)
        │                            │                              │
   Min WPM/Accuracy            Min WPM/Accuracy             Min WPM/Accuracy
        │                            │                              │
   All 8 PASSED?               All 8 PASSED?                All 8 PASSED?
        │                            │                              │
   ► BASIC Exam              ► INTERMEDIATE Exam           ► ADVANCED Exam
        │                            │                              │
   Pass? ► Certificate        Pass? ► Certificate          Pass? ► Certificate
```

Lesson statuses per user: `LOCKED` | `AVAILABLE` | `PASSED` | `FAILED_ATTEMPT`

### 5.2 Placement Test
- Single 60-second typing passage
- WPM determines recommended tier: < 20 → BASIC, 20–39 → INTERMEDIATE, ≥ 40 → ADVANCED
- Sets `placementCompleted=true` and `recommendedTier` on user record
- New users are routed through placement before reaching the dashboard

### 5.3 Typing Engine
- Self-contained React hook (`useTyping`) + display component (`TypingEngine`)
- Tracks per-character state: `correct` / `wrong` / `current` / `pending`
- WPM ticker: recalculates every 300ms while `status === 'running'`
- WPM formula: `(charsTyped / 5) / elapsedMinutes`
- Accuracy formula: `(correctKeys / totalKeys) * 100`
- Backspace disabled — errors cannot be corrected

### 5.4 Admin Panel
- User management: create, deactivate/reactivate, delete, reset password
- Inquiry management: view all user support tickets, post responses, mark resolved
- All admin endpoints require `ROLE_ADMIN` enforced at security filter + `@PreAuthorize`

### 5.5 Support Inquiries
- Users submit subject + message tickets
- Admins respond and mark as `RESOLVED`
- Users can reopen resolved tickets (up to 3 reopens per ticket)

---

## 6. API Design Principles

- All responses are JSON; all errors follow `{ "error": "message" }` shape
- Authentication: `Bearer JWT` header on all protected endpoints
- Admin endpoints prefixed `/api/admin/` — additional `ROLE_ADMIN` check
- Public endpoints (no auth): `POST /api/auth/login`, `POST /api/auth/register`, `/api/auth/forgot-password`, `/api/certificates/public/{id}`
- HTTP verbs: `GET` = read, `POST` = create/action, `PUT` = full update, `DELETE` = remove

---

## 7. Security Architecture

```
HTTP Request
     │
     ▼
JwtAuthFilter ──► Extract Bearer token ──► Validate signature + expiry
     │                                              │
     │                               ─────────────────────────────
     │                               Valid?         │         Invalid?
     │                                 │            │              │
     │                        Set SecurityContext   │        Continue as
     │                                 │            │        unauthenticated
     ▼                                 ▼            │
SecurityFilterChain                 Request         │
  authorizeHttpRequests               │             │
     │                                │             │
  Public path?     ◄──────────────────►        Protected path?
     │                                              │
  Allow through                              Return 401/403
```

Key security components:
- `JwtAuthFilter` — extracts and validates JWT per request
- `UserDetailsServiceImpl` — loads user from DB for Spring Security
- `UserPrincipal` — wraps `User` entity as Spring Security principal
- `SecurityConfig` — defines public vs protected path rules and CORS

---

## 8. Deployment Architecture (Current — Development)

```
Developer Machine (Windows 11)
├── Frontend: npm run dev → Vite on :5173
│   └── Vite proxy: /api/* → http://localhost:8080
└── Backend: mvn spring-boot:run → Spring Boot on :8080
    └── PostgreSQL database: typingtutor (connection via DB_URL env var)

Start scripts: scripts/start-backend.bat, scripts/start-frontend.bat, scripts/start-all.bat
Stop scripts:  scripts/stop-backend.bat, scripts/stop-frontend.bat
```

**Production target (planned):**
- Frontend: Static build (`npm run build`) served via CDN or Nginx
- Backend: JAR deployed to application server, with production DB (PostgreSQL is the production database)
- Environment variables: `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `AI_API_KEY`

---

## 9. Data Flow — Key Scenarios

### Login
```
Browser → POST /api/auth/login {username, password}
        → Spring Security authenticates via UserDetailsServiceImpl
        → JWT generated with username + role claim
        → Login response includes emailVerified and placementCompleted — no separate /me call needed (E-10)
        → AuthContext updated, user redirected to /dashboard
```

### Complete a Lesson
```
User types passage → useTyping tracks chars → status becomes 'finished'
→ TypingEngine calls onComplete(wpm, accuracy)
→ LessonPage calls POST /api/performance {lessonId, wpm, accuracy}
→ PerformanceService saves UserPerformance record
→ LessonService recomputes lesson status (PASSED/FAILED_ATTEMPT)
→ Dashboard refreshes lesson cards
```

### Take Exam
```
User clicks "Take Exam" → ExamPage fetches exam passage from GET /api/exams/{tier}
→ Countdown timer starts → User types passage
→ On complete or timeout → POST /api/exams/{tier}/submit {wpm, accuracy, timeTaken}
→ ExamService checks min thresholds → Pass: Certificate generated → Return certId
→ Fail: Attempt logged, lesson progress reset for tier
```
