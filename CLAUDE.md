# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend
```bash
cd backend
./mvnw spring-boot:run          # Linux/Mac
mvnw.cmd spring-boot:run        # Windows

./mvnw clean package            # build JAR
./mvnw test                     # run all tests
./mvnw test -Dtest=ClassName    # run single test class
```

### Frontend
```bash
cd frontend
npm install     # first time only
npm run dev     # dev server on :5173
npm run build   # production build → dist/
```

## Architecture

### Full-stack layout
```
typing-tutor/
├── backend/   Spring Boot 3.2.5, Java 21, Maven
└── frontend/  React 18, Vite 5, Tailwind CSS, axios
```

Backend serves REST at `:8080`. Vite proxies `/api` → `http://localhost:8080` during development (see [vite.config.js](frontend/vite.config.js)), so all `api.js` calls use `/api` as base URL.

### Backend request flow

```
HTTP request
  → JwtAuthFilter  (extracts Bearer token, sets SecurityContext)
  → SecurityFilterChain  (authorizeHttpRequests rules)
  → @RestController
  → @Service  (business logic, throws IllegalArgumentException / NoSuchElementException)
  → GlobalExceptionHandler  (maps all exceptions to { "error": "..." } JSON)
```

Security rules (in [`SecurityConfig`](backend/src/main/java/com/typingtutor/config/SecurityConfig.java)):
- Public: `POST /api/auth/login`, `POST /api/auth/register`, `/h2-console/**`
- `ROLE_ADMIN` only: `/api/admin/**` (also enforced with `@PreAuthorize("hasRole('ADMIN')")`)
- Everything else: requires a valid JWT

JWT: generated with role claim (`role: "USER"` or `"ADMIN"`). `UserDetailsServiceImpl` maps `user.getRole()` → `ROLE_USER` / `ROLE_ADMIN` for Spring Security's authority system. Token stored client-side in `localStorage` as key `tt_token`.

### Key backend quirks

- **Table names**: `User` maps to `app_users` and its password column is `user_password` — both renamed to avoid H2 reserved-word conflicts.
- **H2 is file-based**, not in-memory: `jdbc:h2:file:./data/typingtutor`. Data persists between restarts. The README says "in-memory" but that is outdated.
- **`ddl-auto=update`** — schema is auto-managed; no migration files needed.
- **LessonService N+1**: `getAllLessonsForUser` makes a per-lesson `findAllByUserIdAndLessonId` call inside a stream. Acceptable at current scale but worth knowing.
- **`AuthResponse`** returns `token`, `username`, `userId`, and `role` — role is used by the frontend to decide admin routing.

### Frontend auth flow

[`AuthContext`](frontend/src/context/AuthContext.jsx) holds the current user and is the single source of truth. On login/register it stores `tt_token` and `tt_user` in `localStorage` and populates `user` state (`{ username, userId, role }`). The axios interceptor in [`api.js`](frontend/src/services/api.js) reads `tt_token` on every request and redirects to `/login` on any 401.

Route guards in [`App.jsx`](frontend/src/App.jsx):
- `ProtectedRoute` — redirects unauthenticated users to `/login`
- `AdminRoute` — redirects non-admins to `/dashboard`
- `PublicRoute` — redirects already-logged-in users to `/dashboard`

### Typing engine

[`useTyping`](frontend/src/hooks/useTyping.js) is a self-contained hook that tracks per-character state (`correct` / `wrong` / `current` / `pending`), drives a 300 ms WPM ticker while `status === 'running'`, and transitions to `finished` when `currentIndex === text.length`. The hook is reset automatically when the `text` prop changes.

WPM formula: `(chars_typed / 5) / elapsed_minutes`. Accuracy: `(correct_keys / total_keys) * 100`.

### Frontend page files

`src/pages/` contains both bare names (`Dashboard.jsx`, `Analytics.jsx`, `Help.jsx`, `Admin.jsx`) and `*Page` versions (`DashboardPage.jsx`, `AnalyticsPage.jsx`, `HelpPage.jsx`, `AdminPage.jsx`). **Only the `*Page` variants are wired into `App.jsx`**; the bare-name files are unused.

### REST API surface

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| POST | `/api/auth/register` | — | Returns JWT + role |
| POST | `/api/auth/login` | — | Returns JWT + role |
| GET | `/api/auth/me` | JWT | Returns username, role, email |
| GET | `/api/lessons` | JWT | All lessons with per-user best WPM/accuracy |
| GET | `/api/lessons/{id}` | JWT | Single lesson |
| POST | `/api/performance` | JWT | Save completed attempt |
| GET | `/api/performance/history` | JWT | All user attempts, newest first |
| POST | `/api/inquiries` | JWT | Submit a support inquiry |
| GET | `/api/inquiries/mine` | JWT | User's own inquiries |
| GET | `/api/admin/users` | ADMIN | List all users |
| POST | `/api/admin/users` | ADMIN | Create user |
| DELETE | `/api/admin/users/{id}` | ADMIN | Delete user + their performance records |
| POST | `/api/admin/users/{id}/reset-password` | ADMIN | Returns temporary password |
| GET | `/api/admin/inquiries` | ADMIN | All inquiries |
| POST | `/api/admin/inquiries/{id}/resolve` | ADMIN | Set response + mark resolved |

### Database schema

```
app_users        (id, username, user_password, role, email)
lessons          (id, title, difficulty_level, content_text, display_order)
user_performance (id, user_id, lesson_id, wpm, accuracy_percentage, completed_at)
inquiries        (id, user_id, subject, message, status, created_at, admin_response)
```

`difficulty_level`: `BASIC` | `INTERMEDIATE` | `ADVANCED` (stored as string).  
`role`: `USER` | `ADMIN` (stored as string).  
`InquiryStatus`: `OPEN` | `RESOLVED`.

24 pre-seeded lessons (8 per tier) loaded via `spring.sql.init.mode=always`.

### H2 console (dev only)
URL: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:file:./data/typingtutor`  
Username: `sa` / Password: *(empty)*
