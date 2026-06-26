---
name: test-coverage-audit
description: Deep test coverage audit — crawls every page, endpoint, and user flow to find untested functionality. Use when asked to check test coverage, find untested features, or identify testing gaps.
---

# Test Coverage Audit

Deep-crawls the entire application — every page, API endpoint, user flow, and error path — to find untested functionality and generate missing tests.

## Quality Rules (MANDATORY)

### Rule 1: Test the user outcome, not the HTTP status
```
BAD:  expect(status).toBe(200)  // Status 200 doesn't mean lesson was saved
GOOD: submit performance -> check stats updated -> lesson shows PASSED
```

### Rule 2: Every form gets a fill+submit+verify test
If a page has inputs, there MUST be a test that fills, submits, and verifies.

### Rule 3: Every mutation gets a verify-after test
- Create → read back, confirm it exists
- Update → read back, confirm value changed
- Delete → read back, confirm it's gone
- Change password → logout → login with new password

### Rule 4: Test across page boundaries (E2E flows)
- Register → verify email → login → see dashboard
- Complete lessons → take exam → earn certificate
- Submit ticket → ticket appears in My Tickets

### Rule 5: Test with the real encryption path
Passwords must be RSA-encrypted in E2E tests, matching the frontend's Web Crypto flow.

### Rule 6: Test what goes wrong, not just what works
For every happy path, test the error path too.

## Audit Process

### Phase 1: Inventory

```bash
# Backend tests
find src/test -name "*.java" | wc -l
find src/test -name "*.java" -exec grep -l "@Test" {} \;

# Frontend E2E tests
find ../typemaster-ui/e2e/tests -name "*.spec.js" | wc -l

# Pages with forms
grep -rl '<input\|<textarea' ../typemaster-ui/src/pages/ | wc -l
```

### Phase 2: Gap Analysis

For each page/feature, check:
- Has at least one test?
- Tests the happy path?
- Tests the error path?
- Tests form fill+submit?
- Tests mutation + verify-after?

### Phase 3: Generate Missing Tests

Backend unit tests: `src/test/java/com/typingtutor/`
Frontend E2E tests: `../typemaster-ui/e2e/tests/`

### Phase 4: Report

```
## Test Coverage Audit
### Date: YYYY-MM-DD
### Summary: X tests (Y backend, Z E2E)

### Gaps Found
| Feature | Test Type | Status | Priority |

### Quality Rule Compliance
| Rule | Compliance | Notes |
```

## Test File Inventory

### Backend (Mockito unit tests)
| File | Covers |
|------|--------|
| UserServiceLoginTest | Login flows |
| UserServiceUpdatePasswordTest | Password change |
| OtpServiceTest | OTP generation/expiry |
| ExamServiceTest | Exam pass/fail |
| LessonServiceTest | Lesson unlock/status |
| PlacementServiceTest | WPM → tier mapping |
| EmailServiceTest | Email send success/failure |
| PasswordCryptoServiceTest | RSA encrypt/decrypt |
| PasswordPolicyTest | Password complexity |
| JwtAuthFilterTest | Invalid token rejection |
| JwtStartupValidatorTest | JWT secret validation |

### Frontend E2E (Playwright)
| File | Covers |
|------|--------|
| 01-auth.spec.js | Login/logout |
| 02-dashboard.spec.js | Dashboard rendering, stats |
| 03-lesson.spec.js | Lesson typing |
| 04-profile.spec.js | Profile page |
| 05-help.spec.js | Help & support |
| 06-certificates.spec.js | Certificate page |
| 07-admin.spec.js | Admin panel |
| 08-exam.spec.js | Exam flow |
| 09-regression.spec.js | Regression checks |
| 10-otp.spec.js | OTP flows |
| 11-placement.spec.js | Placement test |
| 12-analytics.spec.js | Analytics page |
| 13-registration.spec.js | Registration |
| 14-placement.spec.js | Placement extended |
| 15-change-password.spec.js | Password change |
| 16-stats-fixes.spec.js | Dashboard stats + percentile |
