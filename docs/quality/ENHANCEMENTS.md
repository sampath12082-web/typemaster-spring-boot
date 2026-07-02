# TypeMaster — Enhancement Tracker

_Last updated: 2026-07-02_

> **Status key:** ⏳ Pending · 🔄 In Progress · ✅ Done · 🚫 Won't Fix · ⬇️ Deferred

---

| # | Status | Area | Description | Requested | Done |
|---|--------|------|-------------|-----------|------|
| E-1 | ✅ Done | Typing Engine | Disable backspace — errors cannot be corrected mid-lesson; cursor stays in place, wrong chars remain red | 2026-06-12 | 2026-06-12 |
| E-2 | ✅ Done | Audit Logging | Capture full user activity audit log — login/logout timestamps, lesson progress saves, profile changes, password resets, exam submissions; excludes passive browsing | 2026-06-12 | 2026-06-15 |
| E-3 | ✅ Done | Auth | Password strength standards — 16-20 chars + uppercase/lowercase/digit/special enforced on all flows (backend PasswordPolicy, frontend PASSWORD_RE, E2E test fixtures, PasswordStrength component) | 2026-06-12 | 2026-06-17 |
| E-4 | ✅ Done | UX | Contextual tooltips added across 12+ locations — lesson cards, stats bar, dashboard, placement, exam, admin, profile | 2026-06-12 | 2026-06-27 |
| E-5 | ✅ Done | Placement Test | Allow users to skip the placement test — new users skip to BASIC tier lesson 1; returning users skip to continue where they left off | 2026-06-12 | 2026-06-15 |
| E-6 | ✅ Done | Placement Test | Show logout button on the placement test page/modal so users can exit the app without completing placement | 2026-06-12 | 2026-06-17 |
| E-7 | ✅ Done | Security | Encrypt passwords client-side before they leave the browser — request payloads (login, register, change-password, admin create-user) were showing plaintext passwords in DevTools → Network. RSA-OAEP, defense-in-depth on top of TLS. | 2026-06-16 | 2026-06-16 |
| E-8 | ⬇️ Deferred | Auth | Three secret questions for account recovery — requires new entity, schema, 3+ endpoints, and new pages; too large for incremental work. See detail below for full spec. | 2026-06-16 | — |
| E-9 | ✅ Done | Admin | Admin "reset password" now emails an OTP to user's registered address; falls back to 16-char temp password for no-email users with clipboard copy button | 2026-06-16 | 2026-06-17 |
| E-10 | ✅ Done | Performance | Eliminated post-login /me round-trip: AuthResponse now includes emailVerified + placementCompleted; AuthContext.login() skips redundant /me call | 2026-06-16 | 2026-06-17 |
| E-11 | ✅ Done | Help | Help page: "Ticket #X" surfaced in My Tickets list; form area clearer with distinct "Open a support ticket" / "Report a bug" buttons | 2026-06-16 | 2026-06-17 |
| E-12 | ✅ Done | Help | "Report a bug" button added to Help page — pre-fills subject with "[BUG] " prefix, distinct visual style, uses existing Inquiry infrastructure | 2026-06-16 | 2026-06-17 |
| E-13 | ✅ Done | UX | About page added at `/about` — mission, feature grid, tech stack, support link; linked from Navbar | 2026-06-16 | 2026-06-17 |
| E-14 | ✅ Done | Email | `yourtypemaster@gmail.com` now configured as sender in all environments; Gmail SMTP standardized; Brevo references removed | 2026-06-16 | 2026-06-17 |
| E-15 | ✅ Done | Certificates | Certificates now use full name with username fallback in both PDF generation and email notification | 2026-06-16 | 2026-06-17 |
| E-16 | ⬇️ Deferred | Certificates | Certificate visual template blocked — `CertificateService` overlay code is fully wired; only the PNG asset (`src/main/resources/templates/certificate-template.png`) is missing from the repo | 2026-06-16 | — |
| E-17 | ✅ Done | Testing | React unit tests (Vitest + RTL): 12 tests for `useTyping.js` + 4 tests for `TypingEngine.jsx`; setup in `vite.config.js`; `npm test` command added | 2026-07-02 | 2026-07-02 |
| E-18 | ✅ Done | Practice | Configurable backspace in Practice mode — "Backspace" toggle in PracticePage (localStorage `tt_strict_mode`); disabled by default (strict); when on, backspace un-types last char and restores accuracy | 2026-07-02 | 2026-07-02 |
| E-19 | ✅ Done | UX | Smooth animated caret cursor in typing engine — replaced `.char-current` background highlight with a 2px blinking vertical bar (CSS `::before` pseudo-element + `blink` keyframe); caret appears before the current character like Monkeytype | 2026-07-02 | 2026-07-02 |
| E-20 | ✅ Done | Performance | Caffeine cache with TTL replaces ConcurrentMapCacheManager — `leaderboard` cache: 60s TTL, max 1000 entries; `placement` cache: 1h TTL, max 10 entries; RateLimitFilter unbounded-memory risk mitigated | 2026-07-02 | 2026-07-02 |
| E-21 | ✅ Done | AI | Multi-turn HelpAgent conversation memory — frontend sends last 10 messages as history; backend threads them into the Anthropic messages array; chat context maintained across the session without server-side storage | 2026-07-02 | 2026-07-02 |

---

## E-1 · ✅ Done · Disable Backspace During Typing

**Request:** While typing, do not allow backspace to correct errors. Once a character is marked wrong it stays wrong.

**Rationale:** Trains accurate keystrokes rather than correction habits; improves accuracy metric integrity.

**Implementation:** `frontend/src/hooks/useTyping.js`
```js
// Before (9-line block that removed the last typed char and decremented counts)
if (key === 'Backspace') {
  if (currentIndex === 0) return
  ...
}

// After (single no-op return)
if (key === 'Backspace') return
```

**Impact on accuracy formula:** None — `totalKeysRef` is only incremented on non-Backspace keys (guarded by the `key.length > 1 && key !== 'Backspace'` filter on line 42 of useTyping.js, which now also catches Backspace before the increment path).

**Affects:** Lessons, Placement Test, Exam — all use the same `useTyping` hook.

---

## E-2 · ✅ Done · User Activity Audit Log

**Request:** Capture a complete audit trail of meaningful user actions. Passive browsing (page views, scrolling) is excluded.

**Events to capture:**

| Event | Data to store |
|-------|--------------|
| Login | userId, timestamp, IP address (if available), success/failure |
| Logout | userId, timestamp, session duration |
| Password changed | userId, timestamp, changed-by (self or admin), method (self-service / reset / admin-reset) |
| Email updated | userId, timestamp, old email (masked), new email (masked) |
| Profile updated | userId, timestamp, fields changed |
| Lesson completed | userId, lessonId, wpm, accuracy, timestamp |
| Placement test taken | userId, wpm, accuracy, recommendedTier, timestamp |
| Placement test skipped | userId, timestamp |
| Exam submitted | userId, tier, wpm, accuracy, passed, timestamp |
| Certificate generated | userId, certificateId, tier, timestamp |
| Inquiry submitted | userId, inquiryId, subject, timestamp |
| Inquiry reopened | userId, inquiryId, timestamp |
| Admin: user created | adminId, targetUserId, timestamp |
| Admin: user deleted | adminId, targetUserId, timestamp |
| Admin: password reset | adminId, targetUserId, timestamp |
| Admin: user toggled active | adminId, targetUserId, newState, timestamp |
| Admin: inquiry resolved | adminId, inquiryId, timestamp |

**Backend scope (✅ complete as of 2026-06-15):**
- `audit_logs` table with `(id, username, action, details, created_at)` — simpler schema than original spec; `username` = actor for both self-actions and admin-on-user actions
- `AuditLogService.log(username, action, details)` called from: `UserService` (LOGIN, EMAIL_VERIFIED, EMAIL_UPDATED, PASSWORD_CHANGED), `PerformanceService` (LESSON_COMPLETED), `PlacementService` (PLACEMENT_SUBMITTED, PLACEMENT_SKIPPED), `ExamService` (EXAM_SUBMITTED, CERTIFICATE_ISSUED, EXAM_TIER_RESET), `InquiryService` (INQUIRY_SUBMITTED, INQUIRY_REOPENED), `AdminService` (USER_CREATE, USER_DELETE, USER_ACTIVATE, USER_DEACTIVATE, PASSWORD_RESET, INQUIRY_RESOLVED)
- Admin controller exposes `GET /api/admin/audit-logs` (returns latest 200 entries, ADMIN only)
- Admin actions log the actual admin's username (resolved via `@AuthenticationPrincipal` in controller)

**Frontend scope (✅ complete as of 2026-06-27):**
- Admin panel: "Audit Log" tab showing latest 200 entries with refresh button
- User profile: collapsible "My Activity" section showing user's own audit events via `GET /api/auth/my-activity` with action badges, details, and relative timestamps

---

## E-3 · ✅ Done · Password Strength Standards

**Request:** Enforce password complexity on all flows that set a password.

**Update (2026-06-16):** length requirement raised — minimum **16** characters, maximum **20**
characters, aligned to "latest market security standards." This supersedes the original 8-100
char range below; the current `PasswordPolicy` (backend `security/PasswordPolicy.java`) and
`PASSWORD_RE` regex (frontend, duplicated across `RegisterPage.jsx`, `ChangePasswordPage.jsx`,
`ProfilePage.jsx`, `AdminPage.jsx`) both need their length bounds updated to match.

> **Open question for implementation:** "latest market security standards" (e.g. NIST SP 800-63B)
> actually favors *length over composition* — i.e. allowing long passphrases and dropping forced
> uppercase/digit/special-character rules, rather than stacking both a longer minimum length *and*
> composition requirements. Decide at implementation time whether to keep the existing composition
> rules alongside the new 16-20 length window, or relax composition in favor of length alone.

**Rules (original, 8 char minimum — see update above for the new 16-20 window):**
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (`!@#$%^&*()_+-=[]{}|;':\",./<>?`)
- No leading/trailing whitespace

**Affected flows:**
1. Register (`POST /api/auth/register`)
2. Change password (`POST /api/auth/change-password`)
3. Admin: create user (`POST /api/admin/users`) — admin sets initial password
4. Admin: reset password (`POST /api/admin/users/{id}/reset-password`) — generated temp password must meet rules

**Backend scope:**
- Shared `PasswordValidator` utility class with static `validate(password)` returning list of violations
- `@Password` custom JSR-303 annotation for DTO fields
- Applied to `RegisterRequest`, `ChangePasswordRequest`, `AdminCreateUserRequest`
- Generated temp passwords in `AdminService.resetPassword()` already use random chars — verify the charset includes all required categories

**Frontend scope:**
- Password strength indicator component (weak / fair / strong bar) on register and change-password pages
- Inline validation messages per rule (show which rules are unmet in real time)
- Admin create-user modal: show strength indicator when typing new password

---

## E-4 · ✅ Done · Tooltips Throughout Application

**Request:** Add contextual tooltips wherever a UI element is not self-explanatory.

**Completed 2026-06-27:** All 12+ priority tooltip locations implemented using the shared Tooltip component — LessonCard (lock/WPM/accuracy), StatsBar (WPM formula/timer), DashboardPage (exam button), PlacementPage (skip), ExamPage (pass threshold), AdminPage (reset/toggle/delete), ProfilePage (recommended tier).

**Priority tooltip locations:**

| Location | Element | Tooltip text |
|----------|---------|-------------|
| Dashboard — lesson card | Lock icon on LOCKED lesson | "Complete the previous lesson with {minWpm} WPM and {minAccuracy}% accuracy to unlock" |
| Dashboard — lesson card | WPM badge | "Words per minute — your best score on this lesson" |
| Dashboard — lesson card | Accuracy badge | "Percentage of characters typed correctly" |
| Dashboard — tier header | Exam button | "Pass this exam to earn your {tier} certificate" |
| Lesson page | Timer | "Time elapsed since you started typing" |
| Lesson page | WPM counter | "Calculated as (characters typed ÷ 5) ÷ minutes elapsed" |
| Placement test | Skip link | "Skip placement to start from the beginning, or resume where you left off" |
| Exam page | Pass threshold | "You need {minWpm} WPM and {minAccuracy}% accuracy to pass" |
| Admin — users table | Reset password button | "Generate a temporary password; user must change it on next login" |
| Admin — users table | Toggle active | "Deactivated users cannot log in until reactivated" |
| Admin — users table | Delete user | "Permanently deletes the user and all their data. This cannot be undone." |
| Profile — recommended tier | Tier badge | "Based on your placement test result" |
| Register page | Password field | "16-20 characters — see strength meter below" (added 2026-06-16, paired with E-3's new length rule) |
| Change-password (Profile page) | New password field | Same as above — consistent wording with Register |

**Implementation approach:**
- Shared `Tooltip` component wrapping the child element with a `title`-like hover overlay (Tailwind-styled, no library dependency)
- Position: auto-detect viewport edge to flip from bottom to top when near screen bottom
- Delay: 400 ms before showing to avoid flicker on fast mouse passes

---

## E-5 · ✅ Done · Skip Placement Test

**Request:** Users should be able to skip the mandatory placement test. Skipping behaviour differs by user history:

**Completed:** Backend `POST /placement/skip` and frontend skip button in PlacementPage implemented.

| User type | Skip destination |
|-----------|-----------------|
| Brand new user (no performance records) | BASIC tier, lesson 1 (first available lesson) |
| Returning user (has performance records) | Dashboard — resume at their last-unlocked lesson |

**Backend scope (✅ complete as of 2026-06-15):**
- `POST /api/placement/skip` implemented in `PlacementController` + `PlacementService.skipPlacement()`
- Sets `placementCompleted = true`, `recommendedTier = 'BASIC'`, `placementWpm = 0` on the user
- Returns `PlacementResultDto` with `recommendedTier=BASIC` and `startLessonId` pointing to first BASIC lesson
- Emits `PLACEMENT_SKIPPED` audit log entry

**Frontend scope (✅ complete):**
- Add a "Skip placement test" link/button below the start button
- On click: call `POST /api/placement/skip`, update AuthContext (`placementCompleted: true`), redirect to `/dashboard`
- No confirmation needed — skip is a one-time action (no retake endpoint exists)

**Completed:** Frontend skip button in PlacementPage calls `/api/placement/skip`.

---

## E-6 · ✅ Done · Logout Button on Placement Test Page

**Request:** The placement test page/modal currently has no way to exit the application. A logout button must be visible so users can leave without completing placement.

**Completed 2026-06-17:** Logout button added to PlacementPage.

**Scope (`frontend/src/pages/PlacementPage.jsx`):**
- Add a logout button in the top-right corner of the placement page (consistent with Navbar position)
- Button visible at all steps: `intro`, `test`, and `result`
- On click: call `AuthContext.logout()` which clears `tt_token` and `tt_user` from localStorage and redirects to `/login`
- Button should be styled consistently with the existing Navbar logout button (same icon + text or icon-only with tooltip "Logout")
- No confirmation dialog needed — user data is preserved; they can resume or skip placement on next login

---

## E-7 · ✅ Done · Encrypt Password Payloads in Transit

**Request:** DevTools → Network → Request showed plaintext username/password in the login, register,
change-password and admin-create-user request bodies. Encrypt the password before it leaves the
browser so it's never visible in plaintext, even to someone inspecting the request on the user's
own machine (browser extension, screen/network capture, shared support session).

**Note on threat model:** TLS already protects the wire; this is defense-in-depth, not a fix for a
remotely exploitable vulnerability — a password-based login always requires the server to receive
the real password to authenticate it.

**Backend scope (✅ complete):**
- `PasswordCryptoService` (`security/`) generates a fresh RSA-2048 keypair in memory on every
  startup and exposes `decrypt(base64Ciphertext)` (RSA-OAEP/SHA-256).
- `GET /api/auth/public-key` (public, no auth) returns the current public key as Base64 SPKI.
- `AuthController` decrypts `LoginRequest.password`, `RegisterRequest.password`,
  `ChangePasswordRequest.newPassword`, and both fields of `UpdatePasswordRequest` before handing
  plaintext to the unchanged `UserService` methods. `AdminController.createUser` does the same for
  `AdminCreateUserRequest.password`.
- Password complexity (`PasswordPolicy`, extracted from the old DTO `@Pattern` regex) is now
  checked against the decrypted plaintext in the controller, since the wire value is ciphertext
  and can no longer be validated by a DTO-level `@Pattern`.

**Frontend scope (✅ complete):**
- `src/services/crypto.js` — `encryptPassword(plaintext)` fetches the current public key fresh on
  every call (so a backend restart, which rotates the keypair, never leaves a tab encrypting
  against a stale key) and encrypts via the native Web Crypto `RSA-OAEP` API.
- Wired into `AuthContext` (`login`, `register`), `ChangePasswordPage`, `ProfilePage` (change-
  password card), and `AdminPage` (create-user form) — all four password-carrying submissions now
  send ciphertext instead of plaintext.

---

## E-8 · ⬇️ Deferred · Three Secret Questions for Account Recovery

**Request:** Add a second-factor-style security control: 3 secret questions set up by the user,
usable as an alternative path into the forgot-password flow.

**Scope:**
- User picks (or answers) 3 secret questions, likely during registration or as a profile setting
  users can complete later — needs a decision on whether this is mandatory at signup or an
  optional profile addition (open question, not decided here).
- Answer inputs are **masked** (`type="password"`-style) while typing, with a reveal/eye-icon
  toggle — consistent with how password fields already behave elsewhere in the app
  (`PasswordStrength`-adjacent UX, not the same component).
- Answers should be hashed at rest (BCrypt, same as the password column) — never stored or
  compared in plaintext, and never returned to the client once set.
- **Forgot-password integration:** offer "Answer your secret questions instead" as an alternative
  to the existing OTP-based forgot-password flow (`POST /api/auth/forgot-password` →
  `VerifyEmailPage` → `ChangePasswordPage`). Successful answers should grant the same short-lived
  `changePasswordToken` the OTP path already produces, reusing `JwtUtil.generateChangePasswordToken`
  rather than inventing a parallel mechanism.

**Open questions for implementation:** fixed question bank vs. user-authored questions; whether
answers are case/whitespace-normalized before hashing (recommended, to avoid recovery failures
over trivial formatting differences); lockout/rate-limiting on wrong-answer attempts (mirror
`OtpService`'s existing 5-attempt lockout pattern).

---

## E-9 · ✅ Done · Admin Reset Password Sends OTP Instead of a Temp Password

**Request:** `AdminService.resetPassword()` currently generates a random temporary password that
the admin must manually share with the user (see `SECURITY_AUDIT.md` history — temp-password
exposure was already a flagged concern). Replace this with: admin triggers a reset → an OTP is
emailed to the user's **registered** address → user completes the existing OTP → change-password
flow themselves, the same way `RESET_PASSWORD`/`FIRST_LOGIN` OTPs already work.

**Completed 2026-06-17:** Admin reset-password sends OTP to email; falls back to 16-char temp password for no-email users.

**Scope:**
- `AdminController.resetPassword` / `AdminService.resetPassword()` — instead of generating and
  setting a random password directly, call into the existing `OtpService.createOtp(userId,
  "RESET_PASSWORD")` + `EmailService.sendOtp(...)` path and leave the user's current password
  untouched until they complete the OTP flow.
- Decide how this should behave for **no-email users** (admin-created users without an email
  cannot receive an OTP) — likely needs to keep the current temp-password behavior as a fallback
  specifically for that case, consistent with how no-email users already skip OTP elsewhere
  (see B-1/B-2 history in `BUGS.md`).
- Frontend: `AdminPage.jsx`'s "Reset pwd" button/flash message text needs updating to reflect
  "OTP sent to user's email" instead of "temporary password generated."

---

## E-10 · ✅ Done · Improve Login Performance

**Root cause identified:** Frontend `AuthContext.login()` was making a redundant `authApi.me()` call immediately after login to fetch `emailVerified` and `placementCompleted` — two sequential HTTP round-trips just to establish a session. `DashboardPage` then called `/me` again on mount, totalling three `/me` calls for a normal login.

**Fix implemented:**
- `AuthResponse.java` now includes `emailVerified` and `placementCompleted` fields.
- `UserService.login()` and `UserService.verifyEmail()` both pass these values when constructing `AuthResponse`.
- `AuthContext.login()` uses the login response directly — no more post-login `/me` call.
- Net result: login goes from 2 sequential API calls (login + me) to 1 (login only).

**Remaining opportunities (not implemented — document for future work):**
- BCrypt default cost 10 (~100ms/verify) is appropriate for production; no change recommended.
- First-login and forgot-password OTP sends are still synchronous (mail latency appears as login latency). Could be made async with `@Async` / `CompletableFuture`, but needs careful handling since callers currently use the boolean return to decide whether to show `emailWarning`.

**Files:** `AuthResponse.java`, `UserService.java`, `AuthContext.jsx`

---

## E-11 · ✅ Done · Help Module Redesign + Issue Numbers

**Request:** The Help page (`HelpPage.jsx` / `InquiryService`) is cluttered. Segregate content into
clearer sections and make submitted questions/tickets trackable by number.

**Completed 2026-06-17:** Ticket #ID visible in My Tickets list; form area has distinct 'Open a support ticket' and 'Report a bug' buttons.

**Scope:**
- Visually separate FAQ/self-serve content from the "ask a question" submission form and from the
  user's own inquiry history — currently appears to blend together (per `CODE_REVIEW.md` FM-13,
  FAQ answers are also clipped at a fixed height, worth revisiting in the same pass).
- Surface the existing `Inquiry` entity's `id` to the user as a visible "Ticket #123" reference
  when they submit and when they view their inquiry history, so they can reference it in
  follow-up communication.

---

## E-12 · ✅ Done · Let Users Report a Bug

**Request:** Add a distinct "Report a bug" entry point, separate from the general help/question
flow.

**Completed 2026-06-17:** 'Report a bug' button with [BUG] prefix uses existing Inquiry infrastructure.

**Scope:**
- Likely reuses the existing `Inquiry` infrastructure (`InquiryService`, `inquiry_id` ticket
  number from E-11) with an added category/type field (e.g. `BUG_REPORT` vs. general `QUESTION`)
  so admins can filter/triage bug reports separately in the admin Inquiries tab.
- Consider what context to auto-capture with a bug report (current page/route, browser info) to
  reduce back-and-forth — not decided here.

---

## E-13 · ✅ Done · Add an "About" Section

**Request:** Add an About section to the app. Content ideas requested — brainstormed options below
for the user to pick from before implementation:

**Completed 2026-06-17:** AboutPage at /about with mission, feature grid, tech stack, support link. Linked from Navbar.

- **App overview / mission** — what TypeMaster is and why it exists (structured touch-typing
  practice with measurable progress), in 2-3 sentences.
- **How it works** — short visual walkthrough of the placement test → tiered lessons → certification
  exam → certificate pipeline, reusing language already established in `HelpAgentService`'s product
  knowledge system prompt.
- **Support / contact** — ties directly into E-14's support address.
- **Version / what's new** — lightweight changelog of recent features (could pull from this
  `ENHANCEMENTS.md` tracker's "Done" rows rather than maintaining a separate changelog).
- **FAQ link** — cross-link into the redesigned Help section (E-11) rather than duplicating content.
- **Credits/team** — optional, lowest priority.

No decision made yet on which of these to include — needs a follow-up pick before implementation.

---

## E-14 · ✅ Done · Support / OTP Sender Address

**Request:** Use `yourtypemaster@gmail.com` for both the OTP-sending mailbox and the support
contact address shown to users.

**Completed 2026-06-17:** Gmail SMTP (yourtypemaster@gmail.com) standardized across all configs and deploy scripts.

**Scope:**
- `spring.mail.username` / `MAIL_USERNAME` (and corresponding `MAIL_PASSWORD`/app-password) updated
  to this address in all environments (`application.properties`, `application-local.properties`,
  production env vars on Render).
- Any hardcoded support-contact text in the frontend (Help page, About section per E-13, footer if
  any) updated to reference this address.
- Should land together with the B-7 OTP-email fix — no point switching the sender address on a
  send path that's currently broken.

---

## E-15 · ✅ Done · Certificates Should Use Full Name, Not Username

**Request:** Certificates currently print the account username; they should print the user's full
name instead.

**Completed 2026-06-17:** CertificateService uses fullName with username fallback in both PDF and email.

**Scope:**
- `CertificateService`'s PDF generation reads from `User.getFullName()` instead of
  `User.getUsername()` for the printed name field.
- Handle the case where `fullName` is blank/unset (older accounts, or admin-created users) — needs
  a fallback decision (fall back to username vs. block certificate generation until profile is
  completed) — not decided here.

---

## E-16 · ⬇️ Deferred · Restore Previous Certificate Design

**Request:** An earlier certificate visual design/style existed but isn't what's currently being
generated. Review old files and bring the original design back.

**Status:** Overlay code in CertificateService fully wired; blocked on certificate-template.png asset.

**Scope:**
- Look for a prior certificate template/layout (PDFBox drawing code in `CertificateService`, or
  any design assets/mockups) in git history or old project files — current implementation may have
  regressed or simplified the layout at some point.
- Once located, compare against the current `CertificateService` PDF generation and restore the
  original look (fonts, borders/border art, seal/badge placement, color scheme) without
  reintroducing any bugs that prompted the simplification in the first place (check `BUGS.md`/
  `CODE_REVIEW.md` history for any certificate-related fixes before reverting wholesale).

---

_Add new enhancements below using the same format._
