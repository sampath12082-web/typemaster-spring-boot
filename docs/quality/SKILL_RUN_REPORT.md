# Skill Run Report — TypeMaster
### Last Run: 2026-07-02 (follow-up: Flyway + expanded tests) | Pending: 0 items

---

## Sprint: Flyway Baseline + Expanded React Tests (2026-07-02)

| # | Item | Area | Status | Notes |
|---|------|------|--------|-------|
| E-22 | Flyway baseline migration | Production | ✅ Done | Re-added `flyway-core`; real `V1__baseline.sql` with `CREATE TABLE IF NOT EXISTS` for all 9 tables; `baseline-on-migrate=true` makes it safe against existing DBs |
| E-23 | Expanded React unit tests | Production | ✅ Done | 30 tests total (was 20): +4 useTyping edge cases (backspace-at-0, retype-after-backspace, 100% accuracy, text-reset); +10 HelpAgent tests (render, greeting, suggestions, disabled state, history building, escalation) |

**Projected score: ~89/100** (up from 87)

---

## Sprint: 5-Item Score Improvement (2026-07-02)

| # | Item | Area | Status | Notes |
|---|------|------|--------|-------|
| E-17 | React unit tests (Vitest + RTL) | Production | ✅ Done | 16 tests: 12 hook + 4 component, all passing |
| E-18 | Configurable backspace in Practice | Core Functionality | ✅ Done | Toggle persisted to `tt_strict_mode` localStorage |
| E-19 | Smooth caret cursor | Core Functionality | ✅ Done | CSS `::before` blinking caret replaces background highlight |
| E-20 | Caffeine cache with TTL | Performance | ✅ Done | 60s leaderboard TTL + 1h placement TTL; per-cache config |
| E-21 | Multi-turn HelpAgent memory | AI Features | ✅ Done | Last 10 messages sent as history; Anthropic threads context |

**Projected score: ~87/100** (up from 83)

---

### Last Run: 2026-07-02 (all 6 skills in parallel, deep research) | Pending: 0 items

---

## Executive Summary

| Skill | Status | Findings This Run | Action Taken |
|-------|--------|-------------------|--------------|
| 1. Functionality Review | ~~FAIL~~ → **FIXED** | "8 lessons" outdated in 3 places; PracticePage 4-mode description | Fixed all 3: HelpAgent, FAQ, CLAUDE.md |
| 2. App Critique | **PASS 83/100** | +3 pts from 80 → CI test gate confirmed, key drill, analytics | No action needed |
| 3. Doc Writer Audit | ~~FAIL~~ → **FIXED** | 10 doc issues (lesson count, config desc, DB_PASSWORD, CORS, LLD/HLD) | Fixed 8/10 (2 deferred) |
| 4. Project Review | ~~FAIL~~ → **FIXED** | X-Forwarded-For spoofing in RateLimitFilter; leaderboard not actually public | Fixed both critical issues |
| 5. Test Coverage Audit | **PASS** | 18/18 pages covered (PracticePage gap closed), 142 backend tests | PracticePage E2E written |
| 6. Run TypeMaster | **PASS** | 141/141 tests pass, BUILD SUCCESS | — |

---

## Skill 1: Functionality Review

### Result: Fixed (was FAIL)

**Gaps found and fixed this run:**

1. **"8 lessons per tier" outdated in 3 locations** — `data-prod.sql` seeds 12 lessons per tier (not 8). `verifyTierComplete()` checks all non-AI lessons. Three locations corrected:
   - `HelpAgentService.java` system prompt: "Each has 8 lessons" → "Each has 12 lessons"; "all 8 lessons" → "all 12 lessons"
   - `HelpPage.jsx` FAQ: "all 8 lessons" → "all 12 lessons"
   - `CLAUDE.md` lesson model: "Passing all 8 lessons" → "Passing all 12 lessons"

2. **HelpAgent system prompt missing exam thresholds** — Added "Exam thresholds: Basic 25 WPM/85% · Intermediate 40 WPM/87% · Advanced 55 WPM/90%"

3. **PracticePage description in CLAUDE.md incomplete** — Only mentioned 2 modes; updated to all 4:
   - Was: "time (15s/30s/60s/120s) and word count (10/25/50/100) modes"
   - Now: "4 modes: Time (15s/30s/60s/120s), Words (10/25/50/100), Custom Text, Key Drill; word pool selector (200/1K/2K)"

### Pages verified: 18/18 with E2E coverage
### API endpoints: All documented endpoints verified present in controllers
### Enhancement status: All Done items confirmed implemented
### Known bugs: All statuses accurate

---

## Skill 2: App Critique — **83/100** (up from 80/100)

Score progression: 65 → 79.5 → 80 → **83** (after key drill, CI test gate confirmed, analytics)

| Category | Wt | Score | Weighted | Key Evidence |
|----------|----|-------|----------|-------------|
| Core | 15 | 8.5 | 12.75 | 4 practice modes, 3 word pools (2.5K words), gross+net WPM, sound toggle, font-size |
| Curriculum | 15 | 9.0 | 13.50 | 36 lessons (12/tier), placement test, tier unlock, AI-gen, exam→cert flow |
| UX | 10 | 8.5 | 8.50 | 8 accent themes, dark mode, font-size 3-step, sticky nav, toast notifications |
| Security | 10 | 9.0 | 9.00 | RSA-OAEP, Bucket4j, CSP+HSTS, JWT, OTP lockout, email verification gate |
| Analytics | 10 | 8.5 | 8.50 | WPM timeline SVG, keyboard heatmap+finger overlay, activity heatmap, progress rings |
| Certification | 5 | 9.5 | 4.75 | PDF, public verify URL, LinkedIn share, copy-link |
| AI Features | 10 | 8.0 | 8.00 | AI lesson gen (profile-aware), help agent (haiku, structured JSON, fallback) |
| Performance | 5 | 7.5 | 3.75 | Spring Cache (leaderboard+placement), Docker multi-stage, Maven CI cache |
| Mobile | 5 | 8.0 | 4.00 | Tailwind responsive, hamburger nav, mobile accent swatches |
| Production | 15 | 8.0 | 12.00 | 141 tests, CI test gate before deploy, Swagger, Actuator, rate limiting, CSP |

**Total: 84.75 → conservative 83/100**

### What improved since 80/100
- CI pipeline confirmed to run tests before deploy (was previously incorrectly documented as "no test gate")
- Key Drill mode adds genuine differentiation vs market
- Analytics page richness (WPM timeline + finger heatmap + activity heatmap) verified

### Top 3 Strengths
1. **Security depth** — RSA-OAEP + Bucket4j + CSP + HSTS + JwtAuthFilter email gate stack
2. **Analytics richness** — WPM timeline, keyboard heatmap with finger overlay, activity heatmap, tier rings
3. **End-to-end certification** — PDF, public verify, LinkedIn share, email notification

### Top 3 Gaps vs Market
1. No frontend React unit tests (all 141 are backend; Playwright E2E is present but no component-level tests)
2. Typing engine UX: no smooth caret animation; backspace disabled can frustrate beginners
3. Flyway effectively disabled (pom.xml removed dependency) — ddl-auto=update is live schema strategy

---

## Skill 3: Doc Writer Audit

### Result: 8/10 issues fixed (2 deferred)

| # | Doc | Issue | Status |
|---|-----|-------|--------|
| 1 | CLAUDE.md | "8 lessons" → "12 lessons" (lesson unlock threshold) | **FIXED** |
| 2 | CLAUDE.md | DB_PASSWORD default: `postgres` → empty string | **FIXED** |
| 3 | CLAUDE.md | `config/` missing RateLimitFilter, OpenApiConfig, CacheConfig | **FIXED** |
| 4 | CLAUDE.md | CORS default: `http://localhost:*` → `http://localhost:*,http://127.0.0.1:*` | **FIXED** |
| 5 | CLAUDE.md | Deployment: "no test gate before deploy" → tests run first | **FIXED** |
| 6 | CLAUDE.md | `/api/auth/leaderboard` was incorrectly called "public" (not in permitAll) | **FIXED** (added to permitAll in SecurityConfig) |
| 7 | LLD.md | Lesson count "24 pre-seeded via data.sql" → "36 via data-prod.sql" | **FIXED** |
| 8 | LLD.md | `/about` route guard: `(open)` → `ProtectedRoute` | **FIXED** |
| 9 | HLD.md | Tech stack missing Flyway, Bucket4j, springdoc-openapi, Spring Cache | **FIXED** |
| 10 | HLD.md/LLD.md | Dates stale (2026-06-12) | **FIXED** (updated to 2026-07-02) |

---

## Skill 4: Project Review

### Result: Critical issues fixed

**Critical (fixed):**
1. **X-Forwarded-For spoofing** in `RateLimitFilter.resolveClientIp()` — was using first IP (client-controlled), now uses rightmost IP (proxy-appended, Render-trusted). Fix: `parts[parts.length - 1].trim()` instead of `parts[0].trim()`.
2. **`/api/auth/leaderboard` not actually public** — CLAUDE.md said public but SecurityConfig didn't have it in `permitAll()`. Fixed by adding `/api/auth/leaderboard` to `permitAll()` in `SecurityConfig.java`.

**Warnings (deferred / by design):**
- `RateLimitFilter` bucket maps have no TTL eviction — unbounded memory growth under sustained attack. Known risk; Caffeine/Bucket4j ProxyManager would fix this.
- `data-prod.sql` seeds personal family accounts with BCrypt hashes in version control — security concern but intentional user data. No change made without user direction.
- `ExamService` uses `LocalDateTime.now()` for exam timestamps from client-supplied `timeTaken` — minor inconsistency.
- `OtpService` increments attempt counter before checking expiry — suboptimal but not exploitable.

**Passed:**
- No hardcoded secrets in Java source
- Admin double-guard: SecurityConfig + @PreAuthorize
- JWT validation in JwtAuthFilter correct
- GlobalExceptionHandler covers all exception types
- ExamService fail path deletes tier performances
- @Transactional coverage thorough throughout
- @Cacheable on leaderboard and placement correct
- Controllers thin, all business logic in services
- No System.out.println anywhere (SLF4J throughout)

---

## Skill 5: Test Coverage Audit

### Result: **PASS**

**Backend:** 141 @Test methods across 22 files — all 15 services + PasswordPolicy + crypto + security + DTO validation covered

**Frontend E2E:** 21 spec files, ~200 test() calls

### Coverage Matrix — 18/18 pages

| Page | Spec File | Status |
|------|-----------|--------|
| LandingPage | 19-landing.spec.js | COVERED |
| LoginPage | 01-auth.spec.js | COVERED |
| RegisterPage | 01-auth.spec.js + 13-registration.spec.js | COVERED |
| VerifyEmailPage | 10-otp.spec.js | COVERED |
| ChangePasswordPage | 15-change-password.spec.js | COVERED |
| PlacementPage | 11-placement.spec.js + 14-placement.spec.js | COVERED |
| DashboardPage | 02-dashboard.spec.js + 16-stats-fixes.spec.js | COVERED |
| LessonPage | 03-lesson.spec.js | COVERED |
| ExamPage | 08-exam.spec.js | COVERED |
| AnalyticsPage | 12-analytics.spec.js | COVERED |
| LeaderboardPage | 17-leaderboard.spec.js | COVERED |
| CertificatesPage | 06-certificates.spec.js | COVERED |
| CertificateVerifyPage | 18-certificate-verify.spec.js | COVERED |
| HelpPage | 05-help.spec.js | COVERED |
| ProfilePage | 04-profile.spec.js | COVERED |
| AboutPage | 20-about.spec.js | COVERED |
| **PracticePage** | **21-practice.spec.js (NEW)** | **COVERED — gap closed** |
| AdminPage | 07-admin.spec.js | COVERED |

**Quality rule compliance:** All 4 checked rules pass (outcome-based tests, fill+submit+verify, verify-after mutations, RSA encryption in E2E helpers)

---

## Skill 6: Run TypeMaster

### Result: **PASS — 141/141**

All 21 test classes passed cleanly. BUILD SUCCESS. `typing-tutor-backend-1.0.0.jar` produced.

---

## Deferred Items

| Item | Reason | Priority |
|------|--------|----------|
| RateLimitFilter TTL eviction | Needs Caffeine or Bucket4j ProxyManager | Low |
| Personal data in data-prod.sql | User's intentional family accounts — requires user decision | Low |
| ExamService LocalDateTime.now() | Minor inconsistency vs @CreationTimestamp convention | Low |
| OtpService attempt-before-expiry check order | Not exploitable, cosmetic | Low |
| React unit test coverage | 30 tests (useTyping + TypingEngine + HelpAgent); PracticePage component tests still missing | Low |
| Render deploy hook (HTTP 000) | User needs to update RENDER_DEPLOY_HOOK_URL secret | User action |
