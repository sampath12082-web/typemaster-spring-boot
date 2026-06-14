# TypeMaster — Functional & Regression Test Register

_Framework: Playwright (E2E) | Test directory: `frontend/e2e/tests/`_  
_Last updated: 2026-06-12 | Baseline: 110/110 tests passing_

> **Status key:** ✅ Passing · ❌ Failing · ⏭ Skipped · ⚠️ Flaky

---

## How to Run

```bash
cd frontend

# Run all tests (headless)
npx playwright test

# Run a single spec file
npx playwright test e2e/tests/01-auth.spec.js

# Run with browser visible
npx playwright test --headed

# Run and view HTML report
npx playwright test --reporter=html && npx playwright show-report
```

**Pre-requisites:** Backend running on `:8080`, frontend dev server on `:5173`.

---

## Test Summary Dashboard

| Spec File | Area | Tests | Status |
|-----------|------|-------|--------|
| [01-auth.spec.js](#01-authspecjs--authentication) | Authentication | 14 | ✅ All passing |
| [02-dashboard.spec.js](#02-dashboardspecjs--dashboard) | Dashboard | 15 | ✅ All passing |
| [03-lesson.spec.js](#03-lessonspecjs--lesson-page) | Lesson & Typing Engine | 12 | ✅ All passing |
| [04-profile.spec.js](#04-profilespecjs--profile-page) | Profile | 9 | ✅ All passing |
| [05-help.spec.js](#05-helpspecjs--help--support) | Help / Inquiries | 9 | ✅ All passing |
| [06-certificates.spec.js](#06-certificatesspecjs--certificates) | Certificates | 7 | ✅ All passing |
| [07-admin.spec.js](#07-adminspecjs--admin-panel) | Admin Panel | 15 | ✅ All passing |
| [08-exam.spec.js](#08-examspecjs--exam-page) | Exam / Certification | 13 | ✅ All passing |
| [09-regression.spec.js](#09-regressionspecjs--regression-suite) | Regression | 8 | ✅ All passing |
| **Total** | | **102** | **✅ 102/102** |

> Note: Test count may differ from the 110 baseline due to parameterised or conditional tests.

---

## Test Helpers

| Helper file | Purpose |
|-------------|---------|
| `e2e/helpers/api.js` | Programmatic API calls — create/delete users, submit performance, login, register, resolve inquiries, pass lessons in bulk |
| `e2e/helpers/auth.js` | `setAuth(page, user, overrides?)` — injects `tt_token` + `tt_user` into `localStorage` via `addInitScript`, bypassing the login UI for speed |

---

## 01-auth.spec.js — Authentication

**Describe block:** `Authentication` (serial)  
**Setup:** Creates `regularUser` (no-email, admin-created) and `verifiedUser` (self-registered with email, unverified). Cleans up in `afterAll`.

| # | Test | What it verifies |
|---|------|-----------------|
| A-01 | Valid credentials logs in and redirects to `/dashboard` | Happy-path login via UI |
| A-02 | Wrong password shows inline error | Error banner `.bg-red-50` visible on bad credentials |
| A-03 | Inactive user shows `ACCOUNT_INACTIVE` error | Deactivated users cannot log in |
| A-04 | No-email user logs in without OTP — lands on `/dashboard` | **Regression guard:** users without email must NOT be redirected to `/verify-email` |
| A-05 | User with email but unverified is redirected to `/verify-email` | Email-having unverified users still blocked correctly |
| A-06 | Logout clears session and redirects to `/login` | Logout button removes session and redirects |
| A-07 | Unauthenticated access to `/dashboard` redirects to `/login` | `ProtectedRoute` guard works |
| A-08 | Unauthenticated access to `/lesson/1` redirects to `/login` | `ProtectedRoute` guard on lesson deep link |
| A-09 | Logged-in user visiting `/login` is redirected to `/dashboard` | `PublicRoute` guard works |
| A-10 | Non-admin user visiting `/admin` is redirected to `/dashboard` | `AdminRoute` guard works |
| A-11 | Admin user visiting `/admin` loads admin panel | Admin role routing works |
| A-12 | Registration step 1 validates required fields | Empty form → "Full name is required." error |
| A-13 | Registration step 1 validates email format | Invalid email → "Enter a valid email address." error |
| A-14 | Registration step 1 advances to step 2 with valid data | "Step 2 of 2" visible after valid step 1 |
| A-15 | Registration step 2 shows student fields when Student selected | Conditional field rendering for student role |
| A-16 | Registration step 2 shows occupation field when Professional selected | Conditional field rendering for professional role |

---

## 02-dashboard.spec.js — Dashboard

**Describe block:** `Dashboard` (serial)  
**Setup:** Creates `user` (with 3 BASIC lesson performance records submitted via API) and `noEmailUser`. Cleans up in `afterAll`.

| # | Test | What it verifies |
|---|------|-----------------|
| D-01 | Welcome heading shows username | `h1` contains logged-in username |
| D-02 | Shows all three tier sections | Basic, Intermediate, Advanced tier headers visible |
| D-03 | Stats row displays Avg WPM, Lessons Done, Total Runs | KPI stat labels present |
| D-04 | Tier section expands and collapses on toggle | `aria-expanded` toggles on click |
| D-05 | Each tier shows passed/total count | `\d+/8 passed` pattern visible |
| D-06 | Progress bar is visible within each tier | `.bg-emerald-500` bar present |
| D-07 | Lesson cards are rendered inside tier sections | `.card` elements inside tier sections |
| D-08 | Locked lesson cards show 🔒 badge | `🔒 Locked` badge visible |
| D-09 | Available/passed lesson cards show status badge | `Available` badge visible after performance records |
| D-10 | Navbar shows Dashboard, Certificates, Help, Profile links | All nav links present |
| D-11 | Navbar does not show Admin link for regular user | Admin link hidden for non-admins |
| D-12 | Email reminder modal appears for user with no email | "Add your email address" modal shown for no-email users |
| D-13 | Email reminder modal can be dismissed with Remind me later | Dismiss hides the modal |
| D-14 | Email reminder modal Update Now navigates to `/profile` | "Update Now →" button routes to profile |
| D-15 | Email reminder modal does not appear for user with email set | Modal suppressed when email is present |
| D-16 | Quick resume card appears when user has in-progress lessons | "Continue where you left off" visible |

---

## 03-lesson.spec.js — Lesson Page

**Describe block:** `Lesson page` (serial)  
**Setup:** Creates `user`, finds the first `AVAILABLE` lesson via API, stores its `id` and `contentText` for typing simulation.

| # | Test | What it verifies |
|---|------|-----------------|
| L-01 | Lesson card navigates to `/lesson/:id` | Clicking an Available card routes to lesson URL |
| L-02 | Direct URL `/lesson/:id` loads the lesson page | Deep link loads lesson with "Tips:" section |
| L-03 | Lesson page shows breadcrumb with Dashboard link | Breadcrumb button visible |
| L-04 | Breadcrumb Dashboard link navigates back to `/dashboard` | Back navigation works |
| L-05 | Lesson page shows difficulty badge | BASIC / INTERMEDIATE / ADVANCED badge visible |
| L-06 | Typing area renders with `char-current` span | Typing cursor rendered |
| L-07 | Idle hint "Click anywhere and start typing" is shown | Idle state hint visible before first keypress |
| L-08 | Typing correct character marks it `char-correct` (green) | First correct keypress adds `.char-correct` |
| L-09 | Typing wrong character marks it `char-wrong` (red) | Wrong keypress adds `.char-wrong` |
| L-10 | Tab+Enter resets typing engine back to beginning | Reset shortcut clears all typed chars |
| L-11 | Restart button resets the typing engine | ↺ Restart button clears typed chars |
| L-12 | Completing a lesson shows summary modal | Typing full passage → "Lesson Complete!" modal with WPM + Accuracy |
| L-13 | Summary modal Try Again button resets the lesson | Modal closes, typing engine resets |
| L-14 | Summary modal Continue button navigates to dashboard | Continue routes to `/dashboard` |

---

## 04-profile.spec.js — Profile Page

**Describe block:** `Profile page` (serial)  
**Setup:** Creates `user` (with email) and `noEmailUser` (without email).

| # | Test | What it verifies |
|---|------|-----------------|
| P-01 | Profile page loads with correct heading | "My Profile" heading visible |
| P-02 | Username field is shown as read-only | Username rendered as non-editable with `cursor-not-allowed` styling |
| P-03 | Full name field is editable | Name input accepts new value |
| P-04 | Email field is editable | Email input accepts new value |
| P-05 | Student button shows school/class/course fields | Conditional fields for student role |
| P-06 | Professional button shows occupation field and hides school fields | Conditional fields for professional role |
| P-07 | Saving profile shows success message | "Profile saved successfully." toast visible |
| P-08 | No-email user sees info banner about adding email | "Adding an email lets you recover your account" banner visible |
| P-09 | Navbar Profile link navigates to `/profile` | Nav link routes correctly |

---

## 05-help.spec.js — Help & Support

**Describe block:** `Help page` (serial)  
**Setup:** Creates `user`. Inquiries are created via API in individual tests as needed.

| # | Test | What it verifies |
|---|------|-----------------|
| H-01 | Help page loads with main heading | "🆘 Help & Support" heading visible |
| H-02 | FAQ section is visible | "Frequently Asked Questions" section present |
| H-03 | FAQ item expands on click | `aria-expanded="true"` after click |
| H-04 | FAQ item collapses when clicked again | `aria-expanded="false"` after second click |
| H-05 | Only one FAQ item is expanded at a time | At least one `aria-expanded="true"` after two clicks |
| H-06 | Submit form validation requires both fields | Empty submit → "Please fill in both fields." |
| H-07 | Can submit a help inquiry and it appears in My Tickets | Filled form → "Request submitted!" → ticket visible in list |
| H-08 | Submitted ticket shows OPEN status badge | `OPEN` badge present on new ticket |
| H-09 | Resolved ticket shows "Not satisfied? Reopen" button | After admin resolves via API, reopen button visible |
| H-10 | Reopen panel appears when Reopen button clicked | Reason textarea becomes visible |
| H-11 | Reopen requires a reason before submitting | Empty reason → panel stays open (not dismissed) |

---

## 06-certificates.spec.js — Certificates

**Describe block:** `Certificates page` (serial)  
**Setup:** Creates `userNoCerts` (no exams) and `userWithCert` (all BASIC lessons passed + passing exam submitted via API).

| # | Test | What it verifies |
|---|------|-----------------|
| C-01 | Empty state shown when user has no certificates | "No certificates yet" message visible |
| C-02 | Empty state includes link to dashboard | "Go to Dashboard" link present |
| C-03 | Certificate card shows tier name and cert details | "Basic Certificate" + WPM stat visible |
| C-04 | Certificate card has Download PDF button | PDF button rendered |
| C-05 | Certificate card has Copy Link button | "Copy Link" button rendered |
| C-06 | ← Dashboard link navigates back | Link routes to `/dashboard` |
| C-07 | Navbar Certificates link navigates to `/certificates` | Nav link routes correctly |

---

## 07-admin.spec.js — Admin Panel

**Describe block:** `Admin panel` (serial)  
**Setup:** Creates `targetUser` (manage target) and `richUser` (has performance records, used for delete warning test). Logs in as `admin`.

| # | Test | What it verifies |
|---|------|-----------------|
| AD-01 | Admin panel loads with "Admin Panel" heading | Page renders correctly for admin |
| AD-02 | Users tab is shown by default | "👥 Users" tab active on load |
| AD-03 | Admin panel shows KPI cards | Users, Total Runs, Open Tickets cards present |
| AD-04 | User list shows existing users | Created `targetUser` appears in table |
| AD-05 | Target user shows Active status badge | `Active` badge in user row |
| AD-06 | Deactivating a user shows Inactive badge | Deactivate button → `Inactive` badge + "deactivated" toast |
| AD-07 | Reactivating a user shows Active badge again | Activate button → `Active` badge + "activated" toast |
| AD-08 | Reset password shows temporary password to admin | "Temporary password for" toast visible after reset |
| AD-09 | Create user form is visible | Username input + "+ Create User" button present |
| AD-10 | Create user with blank username shows validation error | "Fill in username and email." error shown |
| AD-11 | Create user successfully adds them to the list | Filled form → new user appears in table |
| AD-12 | Delete button opens confirmation dialog | "Delete Permanently" dialog appears |
| AD-13 | Delete dialog for user with runs shows activity warning | "This user has practice activity" warning visible |
| AD-14 | Cancel on delete dialog closes it without deleting | User still in list after cancel |
| AD-15 | Progress tab shows tier progress data | "User Progress Overview" heading visible |
| AD-16 | Inquiries tab shows inquiries section | "Help Inquiries" heading visible |
| AD-17 | Admin can resolve an open inquiry | "Inquiry resolved." toast visible |

---

## 08-exam.spec.js — Exam Page

**Describe block:** `Exam page` (serial)  
**Setup:** Creates four users with different exam states set up via API:
- `userNoLessons` — no lessons passed
- `userLessonsDone` — all BASIC lessons passed, no exam attempt
- `userFailedOnce` — all BASIC lessons passed + 1 failing exam
- `userPassed` — all BASIC lessons passed + 1 passing exam

| # | Test | What it verifies |
|---|------|-----------------|
| E-01 | Exam page shows error when lessons are not all passed | "Exam Not Available" shown |
| E-02 | Passed exam page shows "Already Certified!" message | Certified users see completion state |
| E-03 | Passed exam page shows View Certificate link | Link to certificates visible |
| E-04 | Pre-exam screen shows tier name, duration, min WPM, min accuracy | All exam metadata displayed |
| E-05 | Pre-exam screen shows Start Exam button for first attempt | "Start Exam" button visible |
| E-06 | Pre-exam screen shows attempt counter for user with prior fails | "Retry Exam (Attempt 2 of 3)" button visible |
| E-07 | Attempt tracker circles show previous failed attempts | ✗ symbol in attempt tracker |
| E-08 | Timer warning about not being able to pause is shown | "timer cannot be paused" text visible |
| E-09 | Clicking Start Exam shows timer and typing engine | MM:SS timer + `.char-current` visible |
| E-10 | Dashboard shows certified banner for passed tier | "🏆 Certified" banner visible |
| E-11 | Dashboard shows exam CTA for user with lessons done but not passed | "Take Basic Exam →" CTA visible |
| E-12 | Dashboard shows Try Again CTA for user with failed attempts | "Try Again" CTA visible |

---

## 09-regression.spec.js — Regression Suite

**Describe block:** `Regression: no-email login` (serial)  
**Bug covered:** Users created without an email were blocked with `EMAIL_NOT_VERIFIED` at login and the JWT filter, redirecting them to `/verify-email` with no way to proceed.  
**Fix applied:** `ProtectedRoute` and `VerifiedRoute` now guard on `user.emailVerified === false && user.email` — only redirect if the user **has** an email that is unverified.

| # | Test | What it verifies |
|---|------|-----------------|
| R-01 | **REGRESSION:** No-email user logs in without OTP redirect | UI login → lands on `/dashboard`, NOT `/verify-email` |
| R-02 | **REGRESSION:** No-email user can access `GET /api/lessons` after login | API returns lesson array (not 403) |
| R-03 | **REGRESSION:** No-email user can access `GET /api/exams/status` | API returns exam status array (not 403) |
| R-04 | **REGRESSION:** No-email user can access `GET /api/performance/history` | API returns 200 (not 403) |
| R-05 | **REGRESSION:** No-email user dashboard shows email reminder popup, not OTP page | Dashboard loads + reminder modal shown, no redirect |
| R-06 | User with email but unverified is blocked at login → `/verify-email` | Positive control: email-having unverified users still correctly blocked |
| R-07 | Unverified-email user JWT token is blocked at API level | `POST /api/auth/login` does NOT return a token for unverified users |
| R-08 | Admin with `emailVerified=false` still logs in successfully | Admin role bypasses email verification requirement |

---

## Test Data Strategy

- All test users are created with a `ts = Date.now()` suffix (e.g. `pw_auth_reg_1718000000000`) to avoid username collisions across parallel or repeated runs.
- All users are deleted in `afterAll` hooks. If a test itself creates a user, it deletes it before the test ends.
- Performance records are submitted directly via API (`submitPerformance`) rather than driving the full typing UI — faster and more reliable for setup.
- The `setAuth` helper injects tokens via `addInitScript` (runs before page load), so protected pages open authenticated without going through the login UI.

---

## Tests Pending (Not Yet Implemented)

These flows exist in the application but do not yet have Playwright coverage:

| Area | Missing Test |
|------|-------------|
| Registration | Full end-to-end registration → OTP email → verify → placement → dashboard |
| Placement test | Placement test UI flow (start, type passage, see result, tier assigned) |
| Placement test | Skip placement test (E-5 enhancement — pending implementation) |
| Analytics | Analytics page loads with charts and history data |
| Analytics | Filtering/sorting performance history |
| Admin | Delete user with exam/certificate records (post BM-1 fix) |
| Admin | Create user with no email — verify they land on dashboard, not `/verify-email` |
| Exam | Full exam completion (typing entire passage) — currently only covers pre-exam screen |
| Password change | Forced password change flow (temp password → change-password page → login) |
| OTP | Resend OTP flow |
| Forgot password | Forgot password → OTP → change password flow |
| Certificate | Copy Link button copies correct URL to clipboard |
| Certificate | Public certificate verification page (`/verify/:certId`) |
| Security | OTP rate limiting (SC-4 — pending implementation) |

---

## Known Test Fragilities

| Spec | Test | Risk |
|------|------|------|
| `05-help.spec.js` | H-09 (resolved ticket reopen) | Depends on `resolveInquiry` API helper succeeding before page navigation |
| `07-admin.spec.js` | AD-11 (create user) | Creates a user and cleans it up inline — if the test crashes mid-way, the user may be left in DB |
| `08-exam.spec.js` | E-09 (Start Exam shows timer) | `userLessonsDone` is shared across multiple tests; state changes in one test can affect others if run non-serially |
| `02-dashboard.spec.js` | D-12–15 (email reminder modal) | Relies on `sessionStorage` and `localStorage` being cleared via `addInitScript`; timing-sensitive if SSR or caching changes |
