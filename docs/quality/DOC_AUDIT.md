# Documentation Audit Report
### Date: 2026-06-27

---

## Summary: 15 fixes needed (4 critical, 8 high, 3 medium)

---

## CLAUDE.md

| Finding | Severity |
|---------|----------|
| Missing `UserService` and `PerformanceService` from key services list | High |
| LeaderboardPage, LandingPage, AnalyticsPage undocumented | High |
| `GET /api/auth/leaderboard` endpoint not documented | High |
| Dark mode / ThemeContext not mentioned | High |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` missing from env vars table | High |
| Env var name `CORS_ALLOWED_ORIGIN_PATTERNS` should be `CORS_ALLOWED_ORIGINS` | High |
| Frontend sibling dir says `../frontend` — should be `../typemaster-ui` | High |

## ENHANCEMENTS.md

| Finding | Severity |
|---------|----------|
| E-4 marked Done but only 2/15+ tooltips exist | Critical |
| E-6 marked Pending but is actually implemented | Critical |
| 7 detail sections (E-5, E-9, E-11-E-15) still say "Pending" despite Done | Medium |

## BUGS.md

| Finding | Severity |
|---------|----------|
| B-8 summary says "Deferred" but detail says "Open" — inconsistent | Medium |
| B-8 "depends on B-7" note stale since B-7 is fixed | Medium |

## CODING_STANDARDS.md

| Finding | Severity |
|---------|----------|
| No dark mode conventions (ThemeContext, dark: classes, tt_theme key) | High |
| Password policy (16-20 chars) not documented | High |
| Test baseline says 110 — actual is 156 | Medium |
| Missing `tt_theme` from localStorage keys list | Medium |

## HLD.md / LLD.md

| Finding | Severity |
|---------|----------|
| All H2 references should be PostgreSQL (system diagram, tech stack, config) | Critical |
| Login flow still shows eliminated /me round-trip | Critical |
| Missing `audit_log` table in schema/ERD | High |
| Missing 6 security/service classes from package structure | High |
| Missing 5 API endpoints | High |
| Missing routes: /leaderboard, /about, / (landing) | High |
| No RSA-OAEP encryption documentation | High |
