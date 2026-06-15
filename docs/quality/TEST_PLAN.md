# TypeMaster — Test Plan & Standards

This document defines the testing strategy, suite definitions, tagging conventions, and CI integration guidance for TypeMaster (frontend + backend).

Goals
- Ensure critical user flows always work (smoke).
- Catch regressions across core features (regression/nightly).
- Validate end-to-end behavior (E2E) and component-level correctness (unit/integration).
- Detect flaky tests and enforce reliability thresholds.

Test Types
1. End-to-End (E2E)
   - Framework: Playwright (frontend)
   - Location: `typemaster-reactJS/e2e/tests/`
   - Scope: Full user journeys that cross frontend + backend (auth, lessons, exams, admin workflows).

2. Smoke
   - Small subset of E2E covering the most critical flows:
     - Login (admin and regular user)
     - Open dashboard post-login
     - Open a lesson and render typing area
     - Start exam pre-screen
     - Admin login → Admin Panel loads
   - Execution: run on every PR and merge to main (fast, < 2 minutes ideally).
   - Implementation: Tag smoke tests by including `[SMOKE]` in the test title. Run via `--grep "\\[SMOKE\\]"`.

3. Regression
   - Full E2E + API contract tests. Run nightly or on-demand pre-release.
   - Includes all Playwright specs and backend integration suites.

4. Functional / Integration (Backend)
   - Framework: JUnit + Spring Boot Test / MockMvc
   - Scope: Controller + Service + Repository integration tests that validate API contracts and DB interactions.
   - Location: `typing-tutor-backend/src/test/java/` (add more `@SpringBootTest`/`@WebMvcTest` suites as needed).

5. Unit Tests
   - Fast, isolated tests for service logic, helpers, and DTO validation.

6. Other
   - Security tests (authentication, authorization, JWT handling)
   - Performance smoke (startup time, critical page render time) — optional
   - Accessibility scans (axe) — optional

Tagging & Organization
- Playwright: include tag tokens in titles. Example:
  - `test('SMOKE: valid credentials logs in and redirects to /dashboard', ...)`
  - Run smoke: `npx playwright test --grep "SMOKE:"`
- Backend tests: use JUnit categories (JUnit 5 tags) to mark `@Tag("smoke")` on integration tests.
  - Run smoke backend: `mvn test -Dgroups=smoke` (or use Surefire `-Dtest` patterns)

Stability & Flakiness Policy
- Flaky detection: run tests twice automatically when a failure occurs. If pass on retry, mark as flaky and open issue.
- Flakiness budget: <1% of runs may be flaky on the main branch; enforce via CI gating and a daily flakiness report.
- Fix policy: no flaky test is allowed to remain unannotated for >3 commits. Either fix or remove.

Coverage Targets
- Backend unit/integration coverage: 70%+ (line coverage) as a baseline, 80%+ preferred for critical packages.
- E2E coverage: Not measured as line coverage; instead use feature/API coverage metrics (documented mapping of tests→requirements).

CI Recommendations (GitHub Actions)
- `ci-smoke.yml` (runs on PR):
  - Steps: checkout → start backend (mvnw spring-boot:run in background on 8081) → start frontend (npm run dev in background) → run smoke Playwright tests → collect artifacts
  - Fail fast: if backend or frontend fails to start, abort and notify.
- `ci-regression.yml` (nightly):
  - Run full Playwright suite + backend integration tests with DB initialized from `data-prod.sql` in a disposable PostgreSQL service (container)
- Artifacts: Playwright HTML report, traces (for failures), surefire XMLs, JaCoCo coverage reports

Runbook: How to run locally (short)
- Backend (PowerShell):

```powershell
cd "c:\Users\SAMPAT KUMAR ASEALU\OneDrive\Documents\GitHub\typemaster-spring-boot"
$env:SPRING_BOOT_RUN_ARGS='--server.port=8081'
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="$env:SPRING_BOOT_RUN_ARGS"
```

- Frontend (PowerShell):

```powershell
cd "c:\Users\SAMPAT KUMAR ASEALU\OneDrive\Documents\GitHub\typemaster-reactJS"
npm.cmd run dev
```

- Run smoke Playwright tests (PowerShell):

```powershell
cd "c:\Users\SAMPAT KUMAR ASEALU\OneDrive\Documents\GitHub\typemaster-reactJS"
# Adjust base URL if Vite chose a non-default port
$env:PLAYWRIGHT_BASE_URL='http://localhost:5175'
npm.cmd run test:e2e -- --grep "SMOKE:"
```

Gaps Identified (summary)
- Playwright E2E depends on both frontend and backend running and on fixed ports; CI must orchestrate both services before tests run.
- Some tests fail when the frontend uses a non-default port — tests should rely on env-configurable baseURL (already implemented).
- Backend needs explicit integration test coverage for security-critical flows (OTP lockout, JWT secret validation).
- Missing registration → OTP flow end-to-end tests and placement UI flow tests.

Next actionable items
1. Create smoke tag markers on the most critical tests (add `[SMOKE]` prefix in titles) — frontend repo change.
2. Add a small `ci-smoke.yml` workflow to start both services and run smoke tests.
3. Expand backend integration tests for security-critical flows (OTP rate limiting; JWT secret validation at startup).
4. Implement flakiness detection and nightly regression pipeline.

