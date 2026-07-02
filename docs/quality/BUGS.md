# TypeMaster — Bug Tracker

_Last updated: 2026-07-02_

> **Status key:** ⏳ Open · 🔄 In Progress · ✅ Fixed · 🚫 Won't Fix · ⬇️ Deferred

---

| # | Status | Severity | Area | Summary | Reported | Fixed |
|---|--------|----------|------|---------|----------|-------|
| B-1 | ✅ Fixed | High | Auth | No-email user (sampatk) redirected to `/verify-email` with no way to proceed | 2026-06-12 | 2026-06-12 |
| B-2 | ✅ Fixed | Medium | Auth | Admin password reset set `passwordChanged=false` for no-email users — would trap them if email added later | 2026-06-12 | 2026-06-12 |
| B-3 | ✅ Fixed | High | Auth | Legacy users with existing performance records blocked at `/placement` (DB flag `placementCompleted=false`) | 2026-06-12 | 2026-06-12 |
| B-4 | ✅ Fixed | High | Placement | Placement test auto-submitted with WPM=0 — async call inside React `setState` updater; fired at `c<=1` not `c===0` | 2026-06-12 | 2026-06-12 |
| B-5 | ✅ Fixed | Critical | Admin | Delete user silently failed for users with ExamAttempt/Certificate rows — FK violation → HTTP 500 → list not refreshed | 2026-06-12 | 2026-06-12 |
| B-6 | ✅ Fixed | High | Placement | Placement test always submits WPM=0 and accuracy=0% — metrics not captured during the test session | 2026-06-15 | 2026-06-15 |
| B-7 | ✅ Fixed | High | Email/OTP | Send-OTP email not working — root-caused and permanently fixed (devOtp decoupled, boolean return, Gmail SMTP, emailWarning surfaced) | 2026-06-16 | 2026-06-17 |
| B-8 | ⬇️ Deferred | Testing | Email/OTP | No E2E test coverage for the send-OTP-email flow — blocked until a test mailbox API (Mailtrap/Mailosaur) is available | 2026-06-16 | — |
| B-9 | ✅ Fixed | High | Dashboard | "Lessons Done" stat counted all attempted lessons, not passed ones — showed inflated/inaccurate count | 2026-07-02 | 2026-07-02 |
| B-10 | ✅ Fixed | Medium | Help | Dark mode: "Frequently Asked Questions", "My Tickets", "Help & Support" headings invisible (missing dark: variants) | 2026-07-02 | 2026-07-02 |

---

## Detail

### B-1 · ✅ Fixed · No-email user stuck at `/verify-email`
**Root cause:** `ProtectedRoute` in `AuthContext.jsx:85` redirected any user with `emailVerified=false` to `/verify-email` without checking whether the user actually has an email address to verify.  
**Fix:** Added `&& user.email` guard: `if (user.emailVerified === false && user.email)`. Same fix applied to `VerifiedRoute:105`.  
**Files:** `frontend/src/context/AuthContext.jsx`

---

### B-2 · ✅ Fixed · Admin reset leaves no-email user with wrong `passwordChanged` state
**Root cause:** `AdminService.resetPassword()` always set `passwordChanged=false` (forcing OTP-based change on next login), but no-email users cannot receive OTPs, so they'd be trapped if they ever added an email later.  
**Fix:** `user.setPasswordChanged(!hasEmail)` — no-email users get `passwordChanged=true` since the admin-issued temp password is their final password.  
**Files:** `backend/.../service/AdminService.java`

---

### B-3 · ✅ Fixed · Legacy users blocked at `/placement`
**Root cause:** Users created before the placement system was added have `placementCompleted=false` in the DB. `/api/auth/me` returned this raw value, causing `ProtectedRoute` to redirect them to `/placement` even after years of usage.  
**Fix:** `UserService.isEffectivePlacementCompleted(user)` returns `true` if the user has any `UserPerformance` records. `AuthController.me()` uses this instead of `user.isPlacementCompleted()`.  
**Files:** `backend/.../controller/AuthController.java`, `backend/.../service/UserService.java`, `backend/.../repository/UserPerformanceRepository.java`

---

### B-4 · ✅ Fixed · Placement test fires auto-submit with WPM=0
**Root cause:** `PlacementPage.jsx` called `handleAutoSubmit()` (async network call) inside a `setCountdown(c => {...})` React state updater — an anti-pattern React may invoke multiple times. Also triggered at `c <= 1` (1 second remaining) rather than `c === 0`, so a user finishing in the last second had their actual WPM discarded.  
**Fix:** The `setInterval` now only decrements (`setCountdown(c => c <= 1 ? 0 : c - 1)`). A separate `useEffect` watching `countdown === 0` triggers the submit cleanly.  
**Files:** `frontend/src/pages/PlacementPage.jsx`

---

### B-5 · ✅ Fixed · Admin delete user fails silently for users with exam history
**Root cause:** `AdminService.deleteUser()` deleted only `UserPerformance` and `Inquiry` rows. Users with `ExamAttempt` or `Certificate` rows caused an FK constraint violation (`DataIntegrityViolationException` → HTTP 500). The frontend `catch` block showed "Delete failed" and never refreshed the list.  
**Fix:** Added `deleteByUserId()` to `ExamAttemptRepository` and `CertificateRepository`. `deleteUser()` now deletes in correct FK dependency order: Certificate → ExamAttempt → Performance → Inquiry → User.  
**Files:** `backend/.../service/AdminService.java`, `backend/.../repository/ExamAttemptRepository.java`, `backend/.../repository/CertificateRepository.java`

---

---

### B-6 · ✅ Fixed · Placement test metrics always zero
**Root cause:** On timer expiry, `PlacementPage` called `submitResult(0, 0)` — hardcoded zeros. `TypingEngine` tracked live WPM/accuracy internally but had no way to expose them to the parent until the user finished the full text.  
**Fix:** Added optional `onProgress(wpm, accuracy)` prop to `TypingEngine` that fires on every WPM/accuracy update (every 300ms). `PlacementPage` stores the latest values in `liveMetricsRef` and uses them on timeout auto-submit.  
**Files:** `frontend/src/components/TypingEngine.jsx`, `frontend/src/pages/PlacementPage.jsx`

---

### B-7 · ✅ Fixed · Send-OTP email — permanent fix implemented
**Root cause (multi-factor):**
1. `app.dev-otp-enabled` was not a separate property — it was computed from whether mail was configured. If SMTP credentials were missing in any environment, `devOtp` was returned silently in the API response, leaking OTP codes in non-local environments.
2. `EmailService.send()` swallowed all exceptions without surfacing them to the caller — callers had no way to know sends were failing.
3. Gmail SMTP was inconsistently referenced; some deploy scripts still pointed to Brevo.

**Fix:**
- New `app.dev-otp-enabled` property: `false` in `application.properties` and `application-prod.properties`; `true` only in `application-local.properties`. devOtp is now environment-gated, not mail-configured-gated.
- `EmailService.sendOtp()` and `send()` now return `boolean` — `true` = sent, `false` = disabled or failed (with WARN/ERROR log). All callers surface an `emailWarning` field to the API response on failure instead of silently lying.
- Gmail SMTP (smtp.gmail.com:587, `yourtypemaster@gmail.com`) standardized across `application.properties` and all deploy scripts. Uses Gmail App Password (`MAIL_PASSWORD` env var).
- Frontend `VerifyEmailPage` and `LoginPage` display a red warning banner when `emailWarning` is present, so users know to retry.

**Remaining last mile:** Set `MAIL_PASSWORD=<Gmail App Password>` env var in production (Render dashboard). Code change is complete; without this env var the fix sends correctly in local with App Password but silently skips in production.

**Files:** `EmailService.java`, `UserService.java`, `ProfileUpdateResult.java`, `AuthController.java`, `VerifyEmailPage.jsx`, `LoginPage.jsx`, `RegisterPage.jsx`, `ProfilePage.jsx`, `application*.properties`, `deployment/`

---

### B-8 · ⬇️ Deferred · No real E2E test coverage for send-OTP-email
**Reported:** Existing E2E tests (`10-otp.spec.js` etc.) only exercise the `devOtp` shortcut returned in the API response when `EmailService.isMailEnabled()` is false — they never verify a real email actually gets sent and received.
**Needed:** Frontend-to-email **end-to-end** test coverage, not backend-only: trigger the OTP flow through the real UI (register / resend / forgot-password), and verify an actual email was sent and is usable — e.g. via a test mailbox/inbox API (Mailtrap, Mailosaur, or similar), or at minimum asserting against the mail server logs/send confirmation rather than the `devOtp` bypass. Should run as part of the regular Playwright suite, tagged so it can be skipped in environments without a test-mailbox provider configured.
**Depends on:** B-7 is now fixed; this item remains deferred until a test mailbox provider is configured.

---

### B-9 · ✅ Fixed · "Lessons Done" shows attempted count, not passed count
**Root cause:** `UserPerformanceRepository.countDistinctLessonsByUserId()` counts any lesson with a performance record, regardless of whether the user passed or failed it. "Lessons Done" with a ✅ should reflect passed lessons only.  
**Fix:** Added `countPassedLessonsByUserId()` query (filters `p.wpm >= p.lesson.minWpm AND p.accuracyPercentage >= p.lesson.minAccuracy`). `UserService.getUserStats()` now uses this for `lessonsCompleted`.  
**Files:** `UserPerformanceRepository.java`, `UserService.java`

---

### B-10 · ✅ Fixed · Help page headings invisible in dark mode
**Root cause:** Three static `text-gray-800` / `text-gray-900` headings in HelpPage (`Help & Support` h1, `Frequently Asked Questions` h2, `My Tickets` h2) had no `dark:` variant. In dark mode, these rendered as near-black text on a dark gray card background — effectively invisible.  
**Fix:** Added `dark:text-gray-100` to all three headings.  
**Files:** `HelpPage.jsx`

---

_Add new bugs below using the same format._
