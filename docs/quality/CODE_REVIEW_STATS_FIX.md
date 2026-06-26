# Code Review — Dashboard Stats & Percentile Fix

_Date: 2026-06-23 | Effort: High | Scope: AuthController.java /me endpoint + AnalyticsPage.jsx percentile display_

---

## Findings (8 total, ranked by severity)

### 1. Performance regression on /me endpoint (HIGH)
**File:** `AuthController.java:100`
**Issue:** `/me` is called on every page load. Adding 4 new DB queries (1 redundant user lookup + 3 aggregations) multiplies DB load significantly.
**Failure scenario:** Under moderate concurrency, /me now runs 5 queries instead of 1, increasing DB connection pool pressure and latency on the most frequently called authenticated endpoint.
**Fix:** Accept `User` or `userId` in `getUserStats()` instead of re-querying by username.

### 2. Off-by-one in "Top X%" formula (HIGH)
**File:** `AnalyticsPage.jsx:318`
**Issue:** `100 - percentile + 1` gives wrong results. A user at percentile=50 displays "Top 51%" instead of "Top 50%". At percentile=99, shows "Top 2%" instead of "Top 1%".
**Failure scenario:** The same `+1` error is replicated in `rankingMessage()` at line 136. All percentile displays are off by 1.
**Fix:** Remove the `+ 1`: use `100 - ranking.percentile`.

### 3. Progress bar contradicts label (MEDIUM)
**File:** `AnalyticsPage.jsx:330`
**Issue:** Progress bar width still uses raw `ranking.percentile`% — a 90th-percentile user sees a 90%-full bar but the label reads "Top 11%". The bar metaphor ("fuller = better") is inverted relative to the label ("smaller = better").
**Fix:** Either invert the bar width to `100 - ranking.percentile + 1` or keep the old `%ile` label and ditch `Top X%`.

### 4. Redundant user lookup (MEDIUM)
**File:** `AuthController.java:100`
**Issue:** `getUserStats()` calls `userRepository.findByUsername()` internally, but `/me` already has the `User` object from line 82.
**Fix:** Add an overload `getUserStats(Long userId)` or `getUserStats(User user)`.

### 5. Missing @Transactional(readOnly=true) (MEDIUM)
**File:** `UserService.java:320` (getUserStats)
**Issue:** 4 SELECT queries without a transaction boundary. Stats could be inconsistent if a performance record is inserted between queries.
**Fix:** Add `@Transactional(readOnly = true)` to `getUserStats()`.

### 6. Inconsistent exception type on user-not-found (LOW)
**File:** `AuthController.java:100`
**Issue:** `getUserByUsername` (line 82) throws `NoSuchElementException` (→ 404), but `getUserStats` throws `IllegalArgumentException` (→ 400) for the same scenario.
**Fix:** Make `getUserStats` throw `NoSuchElementException` consistently, or pass the already-fetched `User`.

### 7. GET /me and PUT /me response shape divergence (LOW)
**File:** `AuthController.java:100 vs 124`
**Issue:** GET `/me` now includes stats fields, but PUT `/me` (updateProfile) does not. Latent contract mismatch.
**Fix:** Accepted risk for now — no frontend code uses PUT response for stats.

### 8. Semantic shift without user communication (INFO)
**File:** `AnalyticsPage.jsx:318`
**Issue:** Users previously seeing "95%ile" now see "Top 6%". The number drops dramatically without explanation.
**Note:** Acceptable UX trade-off — "Top X%" is more universally understood.

---

## Action Items

- [ ] Fix off-by-one in percentile formula (remove `+ 1`)
- [ ] Fix progress bar direction or revert to percentile display
- [ ] Optimize /me endpoint: pass User object to getUserStats instead of username
- [ ] Add @Transactional(readOnly=true) to getUserStats
- [ ] Align exception types in getUserStats
