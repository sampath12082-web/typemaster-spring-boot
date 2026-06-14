# TypeMaster — Bug Tracker

_Last updated: 2026-06-12_

> **Status key:** ⏳ Open · 🔄 In Progress · ✅ Fixed · 🚫 Won't Fix · ⬇️ Deferred

---

| # | Status | Severity | Area | Summary | Reported | Fixed |
|---|--------|----------|------|---------|----------|-------|
| B-1 | ✅ Fixed | High | Auth | No-email user (sampatk) redirected to `/verify-email` with no way to proceed | 2026-06-12 | 2026-06-12 |
| B-2 | ✅ Fixed | Medium | Auth | Admin password reset set `passwordChanged=false` for no-email users — would trap them if email added later | 2026-06-12 | 2026-06-12 |
| B-3 | ✅ Fixed | High | Auth | Legacy users with existing performance records blocked at `/placement` (DB flag `placementCompleted=false`) | 2026-06-12 | 2026-06-12 |
| B-4 | ✅ Fixed | High | Placement | Placement test auto-submitted with WPM=0 — async call inside React `setState` updater; fired at `c<=1` not `c===0` | 2026-06-12 | 2026-06-12 |
| B-5 | ✅ Fixed | Critical | Admin | Delete user silently failed for users with ExamAttempt/Certificate rows — FK violation → HTTP 500 → list not refreshed | 2026-06-12 | 2026-06-12 |

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

_Add new bugs below using the same format._
