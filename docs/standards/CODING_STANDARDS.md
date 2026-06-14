# TypeMaster — Coding Standards

_Established: 2026-06-12 | Applies to: Frontend (React 18) + Backend (Spring Boot 3.2.5)_

---

## 1. General Principles

- **No unnecessary comments** — code should be self-documenting through naming. Add a comment only when the *why* is non-obvious (hidden constraint, workaround, subtle invariant).
- **No half-finished implementations** — every feature shipped must be complete end-to-end.
- **No future-proofing** — do not add abstractions for hypothetical future requirements. Three similar lines beat a premature helper.
- **Fail loudly at boundaries** — validate at system edges (user input, external APIs). Trust internal framework guarantees; do not add defensive null checks for values that cannot be null.

---

## 2. Backend — Java / Spring Boot

### 2.1 Naming
| Element | Convention | Example |
|---------|-----------|---------|
| Classes | PascalCase | `UserService`, `JwtAuthFilter` |
| Methods | camelCase | `findByUsername`, `deleteByUserId` |
| Constants | UPPER_SNAKE_CASE | `PASSWORD_CHARS`, `CHANGE_PASSWORD_EXPIRY_MS` |
| DB columns (reserved words) | Aliased via `@Column(name=...)` | `user_password`, `app_users` |
| Enum values | UPPER_SNAKE_CASE | `BASIC`, `INTERMEDIATE`, `ADVANCED` |

### 2.2 Architecture Layers
```
Controller  →  Service  →  Repository  →  Entity
```
- **Controllers** handle HTTP in/out only — no business logic.
- **Services** contain all business logic, throw `IllegalArgumentException` (400) or `NoSuchElementException` (404) for domain errors.
- **GlobalExceptionHandler** maps all exceptions to `{ "error": "..." }` JSON — do not send different error shapes from controllers.
- **Repositories** are Spring Data JPA interfaces — prefer named queries over `@Query` when Spring Data naming conventions suffice.

### 2.3 Transactions
- Any method that writes to the DB must be `@Transactional`.
- Read-only methods benefit from `@Transactional(readOnly = true)` — omit when the method is trivially simple.
- Deletion order in a single transaction must respect FK constraints: delete child rows before parent rows.

### 2.4 Entity Design
- Use Lombok `@Builder` + `@Data` / `@Getter @Setter` — do not write manual getters/setters.
- Reserved H2 words must be renamed via `@Table(name="...")` / `@Column(name="...")`.
- Timestamps: use `@CreationTimestamp` rather than `LocalDateTime.now()` in field initialisers — let the DB own the authoritative time.
- Boolean flags on `User`: `active`, `emailVerified`, `passwordChanged`, `placementCompleted` — all default `false` unless the constructor/builder explicitly sets them.

### 2.5 Security
- All endpoints require JWT unless explicitly listed as `permitAll()` in `SecurityConfig`.
- Admin-only paths use both `SecurityConfig` rule (`/api/admin/**`) **and** `@PreAuthorize("hasRole('ADMIN')")` on the method — defence in depth.
- Sensitive data (passwords, OTPs) must never appear in INFO-level logs.
- User-controlled strings embedded in HTML must be escaped with `HtmlUtils.htmlEscape()`.

### 2.6 Logging
```java
private static final Logger log = LoggerFactory.getLogger(MyClass.class);
```
- Use SLF4J (never `System.out.println`).
- Level guidance: `INFO` for auth events, `WARN` for rejected attempts, `ERROR` for unexpected exceptions, `DEBUG` for OTP/token internal details.
- Production `logging.level.com.typingtutor` must be `INFO`; `DEBUG` is for local development only.

### 2.7 Error Response Shape
All errors return a single consistent shape:
```json
{ "error": "Human-readable message" }
```
HTTP status codes:
| Status | When |
|--------|------|
| 400 | Validation failure, bad input |
| 401 | Missing or invalid JWT |
| 403 | Authenticated but not authorised, or business rule blocked (e.g., email not verified) |
| 404 | Resource not found |
| 500 | Unexpected server error |

---

## 3. Frontend — React / JavaScript

### 3.1 File and Component Naming
| Element | Convention | Example |
|---------|-----------|---------|
| Page components | PascalCase + `Page` suffix | `DashboardPage.jsx`, `LoginPage.jsx` |
| Shared components | PascalCase | `LessonCard.jsx`, `Navbar.jsx` |
| Custom hooks | `use` prefix, camelCase | `useTyping.js`, `useAuth` |
| Context files | `Context` suffix | `AuthContext.jsx`, `ToastContext.jsx` |
| Service files | camelCase | `api.js` |

**Rule:** Only `*Page` files are wired into `App.jsx` routes. Bare-name duplicates (e.g., `Dashboard.jsx`) are unused and must be deleted.

### 3.2 Component Structure
```jsx
// 1. Imports
// 2. Constants / config objects (outside component)
// 3. Component function
//   a. Hooks (useState, useEffect, custom hooks)
//   b. Derived values / memoised values
//   c. Event handlers
//   d. Return JSX
// 4. Sub-components (if small and only used here)
```

### 3.3 Hooks Rules
- **No async calls inside `setState` updater functions** — side-effects in updaters violate React concurrent-mode invariants. Use a separate `useEffect` watching the state value instead.
- **Effects with network calls must be cleaned up** — use `AbortController` or an `isMounted` ref to prevent state updates on unmounted components.
- **Stable references for callbacks in `useEffect` deps** — wrap callbacks passed to effects in `useCallback`; wrap expensive computed values in `useMemo`.
- **`navigate()` must be in a `useEffect`**, never called directly during render.

```jsx
// ✅ Correct — async side-effect watching countdown
useEffect(() => {
  if (countdown === 0 && step === 'test' && !submittedRef.current) {
    submittedRef.current = true
    submitResult(0, 0)
  }
}, [countdown, step])

// ❌ Wrong — async call inside setState updater
setCountdown(c => {
  if (c <= 1) handleAutoSubmit()   // never do this
  return c - 1
})
```

### 3.4 State Management
- `AuthContext` is the single source of truth for the current user — do not store auth state anywhere else.
- `localStorage` keys: `tt_token` (JWT string), `tt_user` (JSON serialised user object).
- `sessionStorage` keys: `tt_email_dismissed` (email reminder dismissal flag).
- Do not store sensitive data (passwords, OTPs) in any browser storage.

### 3.5 API Calls
- All API calls go through `src/services/api.js` — never call `fetch` or `axios` directly in components.
- The axios interceptor attaches `Bearer` token automatically — do not manually attach `Authorization` headers.
- On 401, the interceptor redirects to `/login` — do not add per-call 401 handling in components.
- All API call groups are exported as named objects: `authApi`, `lessonApi`, `performanceApi`, `adminApi`, `placementApi`, `examApi`, `certificateApi`, `inquiryApi`.

### 3.6 Styling
- All styling uses **Tailwind CSS utility classes** — no inline `style` objects except for dynamic values (e.g., `style={{ width: \`${pct}%\` }}`).
- Custom classes (`btn-primary`, `card`, `animate-fade-in`) are defined in `src/index.css` as `@layer components`.
- No external UI libraries — all components are hand-built with Tailwind.

### 3.7 Routing
- `ProtectedRoute` — requires auth + `emailVerified` (when user has email) + `placementCompleted`.
- `AdminRoute` — requires auth + `role === 'ADMIN'`.
- `PublicRoute` — redirects authenticated users to `/dashboard`.
- `VerifiedRoute` — requires auth + `emailVerified` (when user has email); used for `/placement`.

### 3.8 Accessibility Minimums
- `<label>` elements must have `htmlFor` matching the input's `id`.
- Modal/overlay components must have `role="dialog"` and `aria-modal="true"`.
- Active navigation links must use `<NavLink>` (not `<Link>`) so `aria-current="page"` is set automatically.
- Interactive elements must be keyboard-reachable (no `div` click handlers without `tabIndex` + `onKeyDown`).

---

## 4. Git & Version Control

### 4.1 Commit Messages
```
<type>: <short imperative summary>

<body — optional, explains WHY not WHAT>

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
```
Types: `feat`, `fix`, `refactor`, `docs`, `test`, `chore`

### 4.2 Branch Strategy
- `main` — production-ready code only
- Feature/fix branches should be short-lived and PR-merged

### 4.3 What to Never Commit
- `.env` files or files containing secrets
- `target/` build output
- `node_modules/`
- Database files (`*.mv.db`, `*.trace.db`)
- IDE config (`.idea/`, `.vscode/`)

---

## 5. Testing Standards

### 5.1 E2E Tests (Playwright)
- Tests live in `frontend/e2e/tests/`.
- Each spec file covers one feature area (`01-auth.spec.js`, `02-dashboard.spec.js`, etc.).
- Use `getByRole`, `getByLabel` (requires `htmlFor`/`id`), or `getByText` — avoid CSS selectors.
- When a locator matches multiple elements, use `.first()` to resolve strict-mode violations explicitly.
- Do not use comma-separated text selectors (`text=a, text=b`) — Playwright does not treat commas as OR operators.

### 5.2 Target Coverage
- All critical user flows (register → verify → login → lesson → complete) must have E2E tests.
- Admin flows (create user, delete user, reset password, resolve inquiry) must have E2E tests.
- Current baseline: **110/110 Playwright tests passing**.

---

## 6. Environment Configuration

### 6.1 Backend Properties Pattern
```properties
# Required — no default; startup must fail if missing
app.jwt.secret=${JWT_SECRET}

# Optional with safe default
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}

# Feature flag
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
```

### 6.2 Frontend Env Vars
```
VITE_APP_URL=https://typemaster.app   # used for shareable certificate URLs
```
Access via `import.meta.env.VITE_APP_URL`. Never use `process.env` in Vite projects.

### 6.3 Dev vs Production Splits
- H2 console, `spring.sql.init.mode=always`, debug logging — dev only.
- Production must use `spring.jpa.hibernate.ddl-auto=validate` and a real DB.
