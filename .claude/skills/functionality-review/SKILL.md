---
name: functionality-review
description: Review app functionality against documentation. Use when asked to verify features, compare UI with docs, check if implementation matches spec, or audit what's working vs documented.
---

# Functionality Review

Compares the running application's actual behavior against what is documented in `CLAUDE.md` and `docs/`. Identifies gaps where documentation promises something the app doesn't deliver, or the app does something undocumented.

## When to Use

- After implementing a batch of features
- Before a release/handoff
- When user says "this page is broken" or "feature X is missing"
- Periodic health check

## Review Process

### Step 1: Start the App and Authenticate

```bash
# Ensure backend is running on port 8081
curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/auth/public-key

# Get admin auth token (password is RSA-encrypted)
# Use the E2E helper or login via browser
```

### Step 2: Compare Each Page Against CLAUDE.md

Read `CLAUDE.md` and verify each documented feature:

#### Dashboard (`/dashboard`)
- Welcome heading with username
- Stats row: Avg WPM, Lessons Done, Total Runs (from /me endpoint)
- Tier sections (Basic, Intermediate, Advanced) with accordion
- Lesson cards with status badges (Locked, Available, Passed, Failed Attempt)
- Progress bar per tier
- Quick resume card
- Email reminder modal for no-email users

#### My Progress / Analytics (`/analytics`)
- Tier progress rings
- WPM bar chart
- Activity heatmap
- Ranking section (rank, percentile as "Top X%", best WPM)
- Performance history table
- Next target section

#### Leaderboard (`/leaderboard`)
- Global rankings table sorted by best WPM
- Medals for top 3
- Current user highlight
- Avg accuracy and total runs columns

#### Certificates (`/certificates`)
- Certificate cards per tier
- PDF download
- Share verification link

#### Help & Support (`/help`)
- AI assistant (TypeMaster Assistant) with chat interface
- Suggested questions
- Support ticket submission form
- Bug report button
- My Tickets section
- FAQ accordion

#### Profile (`/profile`)
- User info fields (fullName, email, DOB, student, etc.)
- Change password (authenticated flow)
- Email verification status

#### Admin (`/admin`)
- User management (create, delete, toggle active, reset password)
- Inquiry management (view, resolve)

#### Exam (`/exam/:tier`)
- Timed typing test
- Auto-submit on timeout
- Pass/fail result with certificate on pass

#### Lesson (`/lesson/:id`)
- Typing engine with real-time WPM/accuracy
- Char-by-char highlighting (pending, correct, wrong, current)
- Summary modal on completion

### Step 3: Compare API Endpoints Against CLAUDE.md

```bash
# List all actual controller endpoints
grep -rn "@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping" \
  src/main/java/com/typingtutor/controller/ | grep -oP '"[^"]*"' | sort -u
```

### Step 4: Check Enhancement Status Against docs/quality/ENHANCEMENTS.md

```bash
grep "Done\|done" docs/quality/ENHANCEMENTS.md
grep "Open\|open\|Pending\|pending" docs/quality/ENHANCEMENTS.md
```

### Step 5: Run E2E Tests as Functional Verification

```bash
cd ../typemaster-ui/e2e && npx playwright test 2>&1 | tail -5
```

## Report Format

```
## Functionality Review Report
### Date: YYYY-MM-DD

### Pages Verified
| Page | Documented | Actual | Status |
|------|-----------|--------|--------|

### API Gaps
| Documented | Status |

### Enhancement Status Mismatches
| # | Docs Say | Actual |

### Test Results
- Total: X/Y passed

### Recommendations
1. Fix: ...
2. Document: ...
```
