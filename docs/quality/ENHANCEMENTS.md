# TypeMaster — Enhancement Tracker

_Last updated: 2026-06-15_

> **Status key:** ⏳ Pending · 🔄 In Progress · ✅ Done · 🚫 Won't Fix · ⬇️ Deferred

---

| # | Status | Area | Description | Requested | Done |
|---|--------|------|-------------|-----------|------|
| E-1 | ✅ Done | Typing Engine | Disable backspace — errors cannot be corrected mid-lesson; cursor stays in place, wrong chars remain red | 2026-06-12 | 2026-06-12 |
| E-2 | ✅ Done | Audit Logging | Capture full user activity audit log — login/logout timestamps, lesson progress saves, profile changes, password resets, exam submissions; excludes passive browsing | 2026-06-12 | 2026-06-15 |
| E-3 | 🔄 In Progress | Auth | Password strength standards — enforce minimum length, complexity rules on register, change-password, admin create user, and admin reset password flows | 2026-06-12 | — |
| E-4 | ⏳ Pending | UX | Tooltips — add contextual tooltips throughout the application wherever a UI element benefits from a short explanation (lesson lock reasons, WPM/accuracy thresholds, exam rules, admin actions) | 2026-06-12 | — |
| E-5 | ✅ Done | Placement Test | Allow users to skip the placement test — new users skip to BASIC tier lesson 1; returning users skip to continue where they left off | 2026-06-12 | 2026-06-15 |
| E-6 | ⏳ Pending | Placement Test | Show logout button on the placement test page/modal so users can exit the app without completing placement | 2026-06-12 | — |

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

**Frontend scope (⏳ pending):**
- Admin panel: new "Audit Log" tab with filterable, paginated table (filter by user, event type, date range)
- User profile: personal activity history section (own events only, no admin events)

---

## E-3 · 🔄 In Progress · Password Strength Standards

**Request:** Enforce password complexity on all flows that set a password.

**Rules:**
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

## E-4 · ⏳ Pending · Tooltips Throughout Application

**Request:** Add contextual tooltips wherever a UI element is not self-explanatory.

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

**Implementation approach:**
- Shared `Tooltip` component wrapping the child element with a `title`-like hover overlay (Tailwind-styled, no library dependency)
- Position: auto-detect viewport edge to flip from bottom to top when near screen bottom
- Delay: 400 ms before showing to avoid flicker on fast mouse passes

---

## E-5 · ✅ Done · Skip Placement Test

**Request:** Users should be able to skip the mandatory placement test. Skipping behaviour differs by user history:

| User type | Skip destination |
|-----------|-----------------|
| Brand new user (no performance records) | BASIC tier, lesson 1 (first available lesson) |
| Returning user (has performance records) | Dashboard — resume at their last-unlocked lesson |

**Backend scope (✅ complete as of 2026-06-15):**
- `POST /api/placement/skip` implemented in `PlacementController` + `PlacementService.skipPlacement()`
- Sets `placementCompleted = true`, `recommendedTier = 'BASIC'`, `placementWpm = 0` on the user
- Returns `PlacementResultDto` with `recommendedTier=BASIC` and `startLessonId` pointing to first BASIC lesson
- Emits `PLACEMENT_SKIPPED` audit log entry

**Frontend scope (⏳ pending):**
- Add a "Skip placement test" link/button below the start button
- On click: call `POST /api/placement/skip`, update AuthContext (`placementCompleted: true`), redirect to `/dashboard`
- No confirmation needed — the skip endpoint is reversible (user can request another placement via profile settings)

---

## E-6 · ⏳ Pending · Logout Button on Placement Test Page

**Request:** The placement test page/modal currently has no way to exit the application. A logout button must be visible so users can leave without completing placement.

**Scope (`frontend/src/pages/PlacementPage.jsx`):**
- Add a logout button in the top-right corner of the placement page (consistent with Navbar position)
- Button visible at all steps: `intro`, `test`, and `result`
- On click: call `AuthContext.logout()` which clears `tt_token` and `tt_user` from localStorage and redirects to `/login`
- Button should be styled consistently with the existing Navbar logout button (same icon + text or icon-only with tooltip "Logout")
- No confirmation dialog needed — user data is preserved; they can resume or skip placement on next login

---

_Add new enhancements below using the same format._
