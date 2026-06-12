# TypeMaster — Code Review Findings

_Reviewed: 2026-06-12 | Scope: Frontend (React 18) + Backend (Spring Boot 3.2.5)_  
_Last status update: 2026-06-12_

> **Status key:** ⏳ Pending · 🔄 In Progress · ✅ Done · 🚫 Won't Fix · ⬇️ Deferred

---

## Status Dashboard

| Layer | Critical | Major | Minor | Total | Done | Pending |
|-------|----------|-------|-------|-------|------|---------|
| Frontend | 5 | 14 | 9 | 28 | 1 | 27 |
| Backend | 8 | 12 | 13 | 33 | 1 | 32 |
| **Total** | **13** | **26** | **22** | **61** | **2** | **59** |

---

## Frontend

### Critical

| ID | Status | Finding | File |
|----|--------|---------|------|
| FC-1 | ⏳ Pending | Hardcoded production URL — `navigator.clipboard.writeText('https://typemaster.app/...')`. Use `window.location.origin` instead. | `CertificatesPage.jsx:42` |
| FC-2 | ⏳ Pending | Stale closure in TypingEngine — `useEffect` deps missing `onComplete`, `wpm`, `accuracy`. Concurrent re-render before 400ms fires invokes stale callback. | `TypingEngine.jsx:31-35` |
| FC-3 | ⏳ Pending | Hardcoded default password `Pass@123` pre-filled in Create User form. Admin can create accounts with a predictable password. Set `password: ''` + non-empty validation. | `AdminPage.jsx:44` |
| FC-4 | ⏳ Pending | `navigate()` called during render (not in `useEffect`) — fires multiple times in Strict/Concurrent mode. Wrap in `useEffect([changePasswordToken, navigate])`. | `ChangePasswordPage.jsx:23-26` |
| FC-5 | ✅ Done | Async side-effect inside `setState` updater in Placement page. Fixed by moving submit to separate `useEffect` watching `countdown`. ExamPage same pattern still pending. | `PlacementPage.jsx:46-57` / `ExamPage.jsx:46-50` |

### Major

| ID | Status | Finding | File |
|----|--------|---------|------|
| FM-1 | ⏳ Pending | No JWT expiry check on hydration — expired sessions appear logged-in until first 401. Decode `exp` from localStorage token on `AuthProvider` mount. | `AuthContext.jsx:21-27` |
| FM-2 | ⏳ Pending | `Promise.all` with no unmount cleanup — state setters called on unmounted components. Use `AbortController`. | `DashboardPage.jsx:231`, `AnalyticsPage.jsx:128`, `ProfilePage.jsx:18`, `HelpPage.jsx:141`, `CertificatesPage.jsx:19` |
| FM-3 | ⏳ Pending | `chars` array allocated every render — 500 object allocations every 300ms from WPM ticker. Wrap in `useMemo`. | `useTyping.js:106-114` |
| FM-4 | ⏳ Pending | Race condition in ExamPage timer — `handleAutoSubmit` not in `useEffect` dep array (stale closure). | `ExamPage.jsx:43-56` |
| FM-5 | ⏳ Pending | `byTier` object recreated every render — all three `TierSection` components always get new prop references. Wrap in `useMemo`. | `DashboardPage.jsx:244` |
| FM-6 | ⏳ Pending | `loadAll` not memoised — plain function inside component, missing from `useEffect` dep array. Wrap in `useCallback([])`. | `AdminPage.jsx:49-57` |
| FM-7 | ⏳ Pending | `devOtp` forwarded into navigation state — persists in browser history if server accidentally includes it in production. Guard with `import.meta.env.DEV`. | `LoginPage.jsx:41` |
| FM-8 | ⏳ Pending | Null dereference on fetch error — `lesson.difficultyLevel` throws if fetch errors after loading flag clears. Add `if (!lesson) return null`. | `LessonPage.jsx:63` |
| FM-9 | ⏳ Pending | No modal accessibility — missing `role="dialog"`, `aria-modal="true"`, focus trap, Escape handler, focus restoration. | `LessonSummaryModal.jsx:6`, `AdminPage.jsx:8`, `DashboardPage.jsx:20` |
| FM-10 | ⏳ Pending | Paste double-submit in OTP page — native paste fires `onChange` after `handlePaste` already submitted. Add `e.preventDefault()` in paste handler. | `VerifyEmailPage.jsx:70-78` |
| FM-11 | ⏳ Pending | No error handling on analytics fetch — missing `.catch`, network error shows silent empty state. | `AnalyticsPage.jsx:128` |
| FM-12 | ⏳ Pending | Inquiry refresh fire-and-forget — `inquiryApi.mine()` has no `.catch`. Failed refresh silently leaves stale list. | `HelpPage.jsx:141` |
| FM-13 | ⏳ Pending | `max-h-48` clips long FAQ answers (192px cap). Use `max-h-96` or JS-measured height. | `HelpPage.jsx:57` |
| FM-14 | ⏳ Pending | `AdminPage.loadAll` stale closure — function recreated on every render, lint warning suppressed, hides staleness bug. | `AdminPage.jsx:49` |

### Minor

| ID | Status | Finding | File |
|----|--------|---------|------|
| Fm-1 | ⏳ Pending | Wildcard route double-redirects unauthenticated users (`/dashboard` → `/login`). | `App.jsx:42` |
| Fm-2 | ⏳ Pending | Manual dismiss + auto-dismiss fire two parallel cleanup timeouts. Cancel auto-dismiss on early dismiss. | `ToastContext.jsx:32-35` |
| Fm-3 | ✅ Done | Backspace decremented `totalKeys` — inflated accuracy. Fixed by disabling backspace entirely (Enhancement E-1). | `useTyping.js:51` |
| Fm-4 | ⏳ Pending | "Complete lesson N-1" message breaks if `displayOrder` values have gaps. | `LessonCard.jsx:85` |
| Fm-5 | ⏳ Pending | Streak counter uses local browser time vs UTC server timestamps — off-by-one in negative-UTC timezones. | `DashboardPage.jsx:251` |
| Fm-6 | ⏳ Pending | `alert()` for PDF error — rest of app uses `showToast`. | `ExamPage.jsx:89` |
| Fm-7 | ⏳ Pending | `<Link>` not `<NavLink>` — no `aria-current="page"` on active route. | `Navbar.jsx` |
| Fm-8 | ⏳ Pending | `<label>` elements missing `htmlFor`/`id` — clicking label does not focus input. | `LoginPage.jsx:68,79`, `RegisterPage.jsx:99-131`, `ChangePasswordPage.jsx:57,80`, `AdminPage.jsx:152-157` |
| Fm-9 | ⏳ Pending | Confirm no dead bare-name page files (`Dashboard.jsx`, `Analytics.jsx`, etc.) remain as unused exports. | `src/pages/` |

---

## Backend

### Critical

| ID | Status | Finding | File |
|----|--------|---------|------|
| BC-1 | ⏳ Pending | JWT secret hardcoded in source (`PPp27x...`). Anyone with repo access can forge JWTs. Move to env var `${JWT_SECRET}`. | `application.properties:14` |
| BC-2 | ⏳ Pending | H2 console open to internet — `permitAll()` + `frameOptions().disable()` unconditional. Gate on `@Profile("dev")`. | `SecurityConfig.java:50-55` |
| BC-3 | ⏳ Pending | JWT exceptions silently swallowed — tampered/expired tokens fall through unauthenticated. Catch `JwtException` → 401. | `JwtAuthFilter.java:60` |
| BC-4 | ⏳ Pending | OTP compared with `String.equals()` — timing oracle. Use `MessageDigest.isEqual()`. | `OtpService.java:63,81` |
| BC-5 | ⏳ Pending | No OTP brute-force rate limiting — 900,000 combinations enumerable in 30-min window. Add `attempt_count` + 5-failure lockout. | `OtpService.java` |
| BC-6 | ⏳ Pending | Email uniqueness not checked on admin user creation — two accounts with same email breaks OTP delivery. Add `existsByEmail` check. | `AdminService.java:61` |
| BC-7 | ⏳ Pending | Exam scores fully client-supplied — WPM/accuracy not bounded. Add `@Max(300)` on DTO + timeTaken validation. | `ExamController.java:44`, `ExamService.java:85` |
| BC-8 | ⏳ Pending | Email change without re-verification — user can swap to any unregistered email for password reset OTPs. Set `emailVerified=false` on change. | `UserService.java:205-209` |

### Major

| ID | Status | Finding | File |
|----|--------|---------|------|
| BM-1 | ✅ Done | Delete user crashed on FK violation — ExamAttempt/Certificate not deleted first. Fixed: added bulk deletes in correct FK order. | `AdminService.java:93-99` |
| BM-2 | ⏳ Pending | N+1 query in `getAllUsers` — one extra SELECT per user for performance records. Add aggregation query or `@Transactional(readOnly=true)`. | `AdminService.java:40-58` |
| BM-3 | ⏳ Pending | Redundant full-table loads per lesson call — `getLessonById` and `computeLessonStatus` each load all lessons + performances independently. | `LessonService.java:58,78` |
| BM-4 | ⏳ Pending | PDF failure silently produces empty certificate — `generatePdf` returns `byte[0]`, certificate still persisted. Propagate exception to rollback. | `CertificateService.java:183` |
| BM-5 | ⏳ Pending | Race condition on exam attempt count — check-then-act with no DB lock. Two concurrent requests can each pass the "3 attempts" check. | `ExamService.java:94-100` |
| BM-6 | ⏳ Pending | `findLatestUnusedByEmail` throws on multiple unused OTPs — no `LIMIT 1`, `resendOtp` leaves two rows, Spring Data throws `IncorrectResultSizeDataAccessException`. | `EmailVerificationRepository.java:16` |
| BM-7 | ⏳ Pending | CHANGE_PASSWORD token not distinguished from login token — same signing key + claims. Stolen session token usable at `/change-password`. Add `aud` claim. | `JwtUtil.java:42` |
| BM-8 | ⏳ Pending | `@Valid` missing on placement submit — negative WPM/accuracy accepted. | `PlacementController.java:31` |
| BM-9 | ⏳ Pending | No-email admin-created user deadlocked at first login — `email` field has `@Email` but not `@NotBlank`. User gets `passwordChanged=false` but can't receive OTP. | `AdminCreateUserRequest.java:9` |
| BM-10 | ⏳ Pending | `HttpClient` created per request — leaks thread pool. Make it a `private final` field. | `LessonGenerationService.java:127` |
| BM-11 | ⏳ Pending | `purpose` not validated as enum on `resendOtp` — `valueOf()` throws `IllegalArgumentException` on invalid input. Add `@Pattern` validator. | `OtpService.java:43` |
| BM-12 | ⏳ Pending | N+1 deletes for inquiries on user deletion — one DELETE per inquiry. Add `deleteAllByUserId()` bulk query. | `AdminService.java:96-97` |

### Minor

| ID | Status | Finding | File |
|----|--------|---------|------|
| Bm-1 | ⏳ Pending | Token parsed twice per request in `isTokenValid`. Cache `Claims` object. | `JwtUtil.java:77` |
| Bm-2 | ⏳ Pending | OTP logged at INFO in plaintext. Change to DEBUG (or remove code from log). | `EmailService.java:73` |
| Bm-3 | ⏳ Pending | `createdAt = LocalDateTime.now()` at Java construction time. Use `@CreationTimestamp`. | `Inquiry.java:29`, `EmailVerification.java:32` |
| Bm-4 | ⏳ Pending | Same `LocalDateTime.now()` issue. | `Certificate.java:27` |
| Bm-5 | ⏳ Pending | Client `timeTaken` used uncapped — `Integer.MAX_VALUE` produces year ~−67,000 for `startedAt`. Add bounds check. | `ExamService.java:115` |
| Bm-6 | ⏳ Pending | `.size()` on full performance list loaded to memory. Add `countByUserId()` repository method. | `UserService.java:233` |
| Bm-7 | ⏳ Pending | No concurrency guard on `generateNext` — two requests produce duplicate `displayOrder`. | `LessonGenerationService.java` |
| Bm-8 | ⏳ Pending | `localhost:3000` CORS origin hardcoded. Move to `${CORS_ALLOWED_ORIGINS}` property. | `SecurityConfig.java:63` |
| Bm-9 | ⏳ Pending | Catch-all 500 handler does not log the exception. | `GlobalExceptionHandler.java:65` |
| Bm-10 | ⏳ Pending | `@Valid` and `@Size(max)` missing on `ResolveInquiryRequest`. | `AdminController.java:63` |
| Bm-11 | ⏳ Pending | `createUser` not `@Transactional`. | `AdminService.java:61` |
| Bm-12 | ⏳ Pending | Unsafe `(UserPrincipal) userDetails` cast. Use `@AuthenticationPrincipal UserPrincipal` parameter type directly. | `LessonController.java:45`, `ExamController.java:56` |
| Bm-13 | ⏳ Pending | Admin BCrypt hash committed to source — rotate after first deploy. | `data.sql:51` |
