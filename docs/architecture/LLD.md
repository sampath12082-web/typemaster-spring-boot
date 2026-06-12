# TypeMaster — Low Level Design (LLD)

_Version: 1.0 | Last updated: 2026-06-12_

---

## 1. Database Schema

```sql
-- Users
app_users (
  id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
  username              VARCHAR UNIQUE NOT NULL,
  user_password         VARCHAR NOT NULL,           -- BCrypt hash; renamed to avoid H2 reserved word
  role                  VARCHAR NOT NULL,            -- 'USER' | 'ADMIN'
  email                 VARCHAR UNIQUE,
  full_name             VARCHAR,
  date_of_birth         DATE,
  is_student            BOOLEAN DEFAULT FALSE,
  school_name           VARCHAR,
  class_year            VARCHAR,
  course_specialization VARCHAR,
  occupation            VARCHAR,
  is_active             BOOLEAN DEFAULT TRUE,
  email_verified        BOOLEAN DEFAULT FALSE,
  password_changed      BOOLEAN DEFAULT FALSE,
  placement_completed   BOOLEAN DEFAULT FALSE,
  recommended_tier      VARCHAR,                    -- 'BASIC' | 'INTERMEDIATE' | 'ADVANCED'
  placement_wpm         INT
)

-- Lessons (24 pre-seeded via data.sql)
lessons (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  title             VARCHAR NOT NULL,
  difficulty_level  VARCHAR NOT NULL,               -- 'BASIC' | 'INTERMEDIATE' | 'ADVANCED'
  content_text      TEXT NOT NULL,
  display_order     INT NOT NULL,
  min_wpm           INT,
  min_accuracy      DOUBLE
)

-- Performance Records
user_performance (
  id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id             BIGINT NOT NULL → app_users(id),
  lesson_id           BIGINT NOT NULL → lessons(id),
  wpm                 INT NOT NULL,
  accuracy_percentage DOUBLE NOT NULL,
  completed_at        TIMESTAMP
)

-- Email OTP Verification
email_verification (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT NOT NULL → app_users(id),
  otp_code    VARCHAR NOT NULL,
  purpose     VARCHAR NOT NULL,   -- 'VERIFY_EMAIL' | 'FIRST_LOGIN' | 'RESET_PASSWORD'
  used        BOOLEAN DEFAULT FALSE,
  created_at  TIMESTAMP,
  expires_at  TIMESTAMP
)

-- Exams (one per tier, seeded)
exams (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  tier             VARCHAR NOT NULL UNIQUE,
  passage          TEXT NOT NULL,
  min_wpm          INT NOT NULL,
  min_accuracy     DOUBLE NOT NULL,
  duration_minutes INT NOT NULL
)

-- Exam Attempts
exam_attempts (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id      BIGINT NOT NULL → app_users(id),
  exam_id      BIGINT NOT NULL → exams(id),
  wpm          INT,
  accuracy     DOUBLE,
  time_taken   INT,
  passed       BOOLEAN DEFAULT FALSE,
  completed_at TIMESTAMP
)

-- Certificates
certificates (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id         BIGINT NOT NULL → app_users(id),
  exam_attempt_id BIGINT NOT NULL → exam_attempts(id),
  certificate_id  VARCHAR UNIQUE NOT NULL,           -- UUID for public verification
  tier            VARCHAR NOT NULL,
  wpm             INT,
  accuracy        DOUBLE,
  pdf_data        BLOB,
  issued_at       TIMESTAMP
)

-- Support Inquiries
inquiries (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id        BIGINT NOT NULL → app_users(id),
  subject        VARCHAR NOT NULL,
  message        TEXT NOT NULL,
  status         VARCHAR NOT NULL,                   -- 'OPEN' | 'RESOLVED' | 'REOPENED'
  admin_response TEXT,
  reopen_count   INT DEFAULT 0,
  created_at     TIMESTAMP
)
```

**Delete cascade order (required for user deletion):**
`certificates` → `exam_attempts` → `user_performance` → `inquiries` → `app_users`

---

## 2. Entity Relationship Diagram

```
app_users ──┬──< user_performance >── lessons
            ├──< email_verification
            ├──< inquiries
            ├──< exam_attempts >──── exams
            └──< certificates >──── exam_attempts
```

---

## 3. Backend Component Design

### 3.1 Package Structure
```
com.typingtutor
├── config/
│   └── SecurityConfig.java          JWT filter chain, CORS, public paths
├── controller/
│   ├── AuthController.java          /api/auth/*
│   ├── LessonController.java        /api/lessons/*
│   ├── PerformanceController.java   /api/performance/*
│   ├── PlacementController.java     /api/placement/*
│   ├── ExamController.java          /api/exams/*, /api/certificates/*
│   ├── InquiryController.java       /api/inquiries/*
│   ├── AdminController.java         /api/admin/*
│   └── GlobalExceptionHandler.java  @RestControllerAdvice
├── service/
│   ├── UserService.java             Auth, register, login, profile
│   ├── LessonService.java           Lesson listing, status computation
│   ├── PerformanceService.java      Save attempt, lesson locking logic
│   ├── PlacementService.java        Placement passage, tier determination
│   ├── ExamService.java             Exam submission, pass/fail, tier reset
│   ├── CertificateService.java      Certificate generation, PDF, public lookup
│   ├── AdminService.java            User CRUD, inquiry management
│   ├── OtpService.java              OTP creation, validation, expiry
│   ├── EmailService.java            SMTP sending (welcome, OTP emails)
│   └── LessonGenerationService.java AI-generated lesson content
├── security/
│   ├── JwtUtil.java                 Token generation, claim extraction
│   ├── JwtAuthFilter.java           OncePerRequestFilter, sets SecurityContext
│   ├── UserPrincipal.java           Spring Security UserDetails wrapper
│   └── UserDetailsServiceImpl.java  Loads user from DB for authentication
├── entity/                          JPA entities (User, Lesson, etc.)
├── dto/                             Request/response data transfer objects
├── repository/                      Spring Data JPA interfaces
└── TypingTutorApplication.java      Spring Boot entry point
```

### 3.2 Request Processing Pipeline
```
HTTP Request
     │
     ▼
CorsFilter                          ← CORS headers added
     │
     ▼
JwtAuthFilter.doFilterInternal()
  ├── Extract Authorization header
  ├── Parse Bearer token via JwtUtil
  ├── Load UserDetails via UserDetailsServiceImpl
  ├── Set UsernamePasswordAuthenticationToken in SecurityContext
  └── Continue chain (or return 401 on exception)
     │
     ▼
SecurityFilterChain.authorizeHttpRequests()
  ├── Public paths: allow
  ├── /api/admin/**: require ROLE_ADMIN
  └── Everything else: require authenticated
     │
     ▼
@RestController method
     │
     ▼
@Service method
  └── Business logic; throws IllegalArgumentException / NoSuchElementException
     │
     ▼
GlobalExceptionHandler
  └── Maps all exceptions → { "error": "..." } JSON with correct HTTP status
```

### 3.3 JWT Token Design

**Login token claims:**
```json
{
  "sub": "username",
  "role": "USER" | "ADMIN",
  "iat": 1718000000,
  "exp": 1718086400
}
```

**Change-password token claims:**
```json
{
  "sub": "username",
  "purpose": "CHANGE_PASSWORD",
  "iat": 1718000000,
  "exp": 1718000900    // 15-minute expiry
}
```

Signing algorithm: HS256. Secret loaded from `${JWT_SECRET}` env var.

### 3.4 Lesson Status Algorithm (`LessonService`)
```
For each lesson (ordered by displayOrder):
  performances = findAllByUserIdAndLessonId(userId, lessonId)
  
  if performances is empty:
    if lesson is the first lesson in tier → AVAILABLE
    if previous lesson is PASSED → AVAILABLE
    else → LOCKED
  
  if any performance meets min WPM AND min accuracy → PASSED
  else → FAILED_ATTEMPT
```

---

## 4. Frontend Component Design

### 4.1 Route Map
```
/ ─────────────────────────────────────────► /dashboard (wildcard redirect)
/login          PublicRoute     LoginPage
/register       PublicRoute     RegisterPage
/verify-email   (open)          VerifyEmailPage
/change-password (open)         ChangePasswordPage
/placement      VerifiedRoute   PlacementPage
/dashboard      ProtectedRoute  DashboardPage
/lesson/:id     ProtectedRoute  LessonPage
/analytics      ProtectedRoute  AnalyticsPage
/help           ProtectedRoute  HelpPage
/exam/:tier     ProtectedRoute  ExamPage
/certificates   ProtectedRoute  CertificatesPage
/profile        ProtectedRoute  ProfilePage
/admin          AdminRoute      AdminPage
```

### 4.2 Route Guard Logic
```
ProtectedRoute:
  !user → /login
  user.emailVerified === false && user.email → /verify-email
  user.placementCompleted === false → /placement
  → render children

AdminRoute:
  !user → /login
  user.role !== 'ADMIN' → /dashboard
  → render children

PublicRoute:
  user exists → /dashboard
  → render children

VerifiedRoute:
  !user → /login
  user.emailVerified === false && user.email → /verify-email
  → render children
```

### 4.3 AuthContext State Shape
```javascript
user: {
  username:           string,
  userId:             number,
  role:               'USER' | 'ADMIN',
  email:              string,          // '' if no email
  fullName:           string,
  emailVerified:      boolean,
  placementCompleted: boolean,
  recommendedTier:    string | null,   // 'BASIC' | 'INTERMEDIATE' | 'ADVANCED'
}
```

Persisted to `localStorage` as `tt_user` (JSON). Rehydrated on app load via `parseUser()` which provides safe defaults:
```javascript
emailVerified:      data.emailVerified ?? true,      // legacy users default to verified
placementCompleted: data.placementCompleted ?? true,  // legacy users default to completed
```

### 4.4 Typing Engine State Machine
```
                    first keypress
idle ─────────────────────────────────────► running
                                               │
                              countdown=0 or   │ all chars typed
                              user completes    │
                                               ▼
                                           finished
                                               │
                                 text prop changes (reset)
                                               │
                                               ▼
                                             idle
```

State tracked in `useTyping` hook:
```javascript
typedChars:   Array<{ char, correct }>   // per-character result
currentIndex: number                      // cursor position
status:       'idle' | 'running' | 'finished'
wpm:          number                      // recalculated every 300ms
accuracy:     number                      // correctKeys / totalKeys * 100
```

### 4.5 API Service Layer (`src/services/api.js`)
```javascript
// Axios instance
api = axios.create({ baseURL: '/api' })

// Request interceptor: attach JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('tt_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Response interceptor: handle 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('tt_token')
      localStorage.removeItem('tt_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// API groups
authApi        → /api/auth/*
lessonApi      → /api/lessons/*
performanceApi → /api/performance/*
placementApi   → /api/placement/*
examApi        → /api/exams/*
certificateApi → /api/certificates/*
inquiryApi     → /api/inquiries/*
adminApi       → /api/admin/*
```

---

## 5. REST API Reference

### Auth
| Method | Path | Auth | Request | Response |
|--------|------|------|---------|---------|
| POST | `/api/auth/register` | — | `{username, email, password, fullName, ...}` | `{message, email, devOtp?}` |
| POST | `/api/auth/login` | — | `{username, password}` | `{token, username, userId, role}` or `{requiresPasswordChange, email}` |
| POST | `/api/auth/verify-email` | — | `{email, otp}` | `{token, username, userId, role}` |
| POST | `/api/auth/resend-otp` | — | `{email, purpose}` | `{message}` |
| POST | `/api/auth/verify-otp` | — | `{email, otp, purpose?}` | `{changePasswordToken}` |
| POST | `/api/auth/change-password` | — | `{changePasswordToken, newPassword}` | `{message}` |
| POST | `/api/auth/forgot-password` | — | `{email}` | `{message}` |
| GET | `/api/auth/me` | JWT | — | Full user profile object |
| PUT | `/api/auth/me` | JWT | Profile fields | Updated profile |

### Lessons
| Method | Path | Auth | Response |
|--------|------|------|---------|
| GET | `/api/lessons` | JWT | Array of lessons with `status`, `bestWpm`, `bestAccuracy` per user |
| GET | `/api/lessons/{id}` | JWT | Single lesson with status |
| POST | `/api/lessons/generate-next` | JWT | Generated AI lesson |

### Performance
| Method | Path | Auth | Request | Response |
|--------|------|------|---------|---------|
| POST | `/api/performance` | JWT | `{lessonId, wpm, accuracy}` | Saved performance record |
| GET | `/api/performance/history` | JWT | — | All user attempts, newest first |

### Placement
| Method | Path | Auth | Response |
|--------|------|------|---------|
| GET | `/api/placement/test` | JWT | `{passage, timeLimitSeconds}` |
| POST | `/api/placement/submit` | JWT | `{wpm, accuracy}` → `{recommendedTier, startLessonId, wpm, accuracy}` |

### Exams & Certificates
| Method | Path | Auth | Response |
|--------|------|------|---------|
| GET | `/api/exams/{tier}` | JWT | Exam details + passage |
| POST | `/api/exams/{tier}/submit` | JWT | `{wpm, accuracy, timeTaken}` → `{passed, wpm, accuracy, certId?}` |
| GET | `/api/certificates` | JWT | User's certificates list |
| GET | `/api/certificates/{certId}/pdf` | JWT | PDF blob |
| GET | `/api/certificates/public/{certId}` | — | Public certificate verification data |

### Help / Inquiries
| Method | Path | Auth | Response |
|--------|------|------|---------|
| POST | `/api/inquiries` | JWT | `{subject, message}` → Saved inquiry |
| GET | `/api/inquiries/mine` | JWT | User's own inquiries |
| POST | `/api/inquiries/{id}/reopen` | JWT | `{reason}` → Updated inquiry |

### Admin
| Method | Path | Auth | Response |
|--------|------|------|---------|
| GET | `/api/admin/users` | ADMIN | All non-admin users with stats |
| POST | `/api/admin/users` | ADMIN | `{username, email, password}` → Created user |
| DELETE | `/api/admin/users/{id}` | ADMIN | 200 OK |
| POST | `/api/admin/users/{id}/reset-password` | ADMIN | `{temporaryPassword}` |
| PATCH | `/api/admin/users/{id}/toggle-active` | ADMIN | `{active: bool}` |
| GET | `/api/admin/inquiries` | ADMIN | All inquiries |
| POST | `/api/admin/inquiries/{id}/resolve` | ADMIN | `{response}` → Resolved inquiry |

---

## 6. Key Sequence Diagrams

### 6.1 First-Time User Registration → Lesson
```
Browser          Backend              Email Server
   │                │                      │
   │─ POST /register ──────────────────────►│
   │                │── Send OTP email ────►│
   │◄─ {message, devOtp?} ─────────────────│
   │                │                      │
   │─ POST /verify-email (otp) ────────────►│
   │◄─ {token} ────────────────────────────│
   │                │                      │
   │  [Store token in localStorage]
   │                │
   │─ GET /api/auth/me ──────────────────►│
   │◄─ {emailVerified, placementCompleted=false} ───│
   │                │
   │  [AuthContext → placementCompleted=false → redirect /placement]
   │                │
   │─ GET /api/placement/test ──────────►│
   │◄─ {passage, timeLimitSeconds} ──────│
   │                │
   │  [User types → POST /api/placement/submit]
   │◄─ {recommendedTier, wpm} ──────────│
   │                │
   │  [updateUser({placementCompleted: true}) → redirect /dashboard]
   │                │
   │─ GET /api/lessons ──────────────────►│
   │◄─ [lesson array with status] ────────│
```

### 6.2 Admin Delete User
```
AdminPage        Backend
    │               │
    │─ DELETE /api/admin/users/{id} ──────────────►│
    │               │
    │               │  BEGIN TRANSACTION
    │               │  DELETE FROM certificates WHERE user_id = ?
    │               │  DELETE FROM exam_attempts WHERE user_id = ?
    │               │  DELETE FROM user_performance WHERE user_id = ?
    │               │  DELETE FROM inquiries WHERE user_id = ?  (N queries)
    │               │  DELETE FROM app_users WHERE id = ?
    │               │  COMMIT
    │               │
    │◄─ 200 OK ─────│
    │               │
    │  [loadAll() → refresh users list]
```

---

## 7. Configuration Reference

### Backend (`application.properties`)
| Property | Default | Purpose |
|----------|---------|---------|
| `server.port` | 8080 | API server port |
| `app.jwt.secret` | *(must be env var)* | JWT signing secret |
| `app.jwt.expiration-ms` | 86400000 (24h) | JWT lifetime |
| `spring.datasource.url` | `jdbc:h2:file:./data/typingtutor` | DB location |
| `spring.h2.console.enabled` | true | H2 web console (dev only) |
| `spring.jpa.hibernate.ddl-auto` | update | Auto-schema management |
| `spring.sql.init.mode` | always | Run data.sql on startup |
| `app.placement.passage` | *(built-in text)* | Placement test passage |
| `app.placement.time-seconds` | 60 | Placement timer |
| `spring.mail.host` | smtp.gmail.com | SMTP server |
| `app.ai.api-key` | *(env var)* | AI lesson generation key |
| `logging.level.com.typingtutor` | DEBUG | App log level (INFO in prod) |

### Frontend (`.env` / Vite)
| Variable | Example | Purpose |
|----------|---------|---------|
| `VITE_APP_URL` | `https://typemaster.app` | Base URL for shareable certificate links |

### Vite Proxy (`vite.config.js`)
```javascript
proxy: {
  '/api': { target: 'http://localhost:8080', changeOrigin: true }
}
```
All `/api/*` requests from the browser are proxied to the Spring Boot backend during development.
