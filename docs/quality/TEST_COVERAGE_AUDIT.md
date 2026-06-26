# Test Coverage Audit
### Date: 2026-06-27

---

## Summary

| Metric | Count |
|--------|-------|
| Backend test files | 15 (73 @Test methods) |
| Frontend E2E specs | 16 (156 tests) |
| Backend services with tests | 6/15 (40%) |
| Frontend pages with E2E | 14/17 (82%) |
| API endpoints with zero coverage | 3/39 (8%) |

---

## Backend Services Coverage

| Service | Unit Tests | Count | Status |
|---------|-----------|-------|--------|
| PasswordPolicy | Yes | 13 | Excellent |
| UserService (login) | Partial | 8 | register/profile/ranking untested |
| OtpService | Yes | 7+1 | Good |
| UserService (password) | Yes | 6 | Good |
| EmailService | Yes | 6 | Good |
| ExamService | Yes | 5 | Good |
| LessonService | Yes | 4 | OK |
| PlacementService | Yes | 4 | OK |
| PasswordCryptoService | Yes | 2 | OK |
| AdminService | **No** | 0 | CRITICAL GAP |
| CertificateService | **No** | 0 | HIGH GAP |
| PerformanceService | **No** | 0 | HIGH GAP |
| InquiryService | **No** | 0 | MEDIUM GAP |
| HelpAgentService | **No** | 0 | MEDIUM GAP |
| LessonGenerationService | **No** | 0 | MEDIUM GAP |
| AuditLogService | **No** | 0 | LOW |
| UserDetailsServiceImpl | **No** | 0 | LOW |

## Frontend Pages Coverage

| Page | E2E Coverage | Status |
|------|-------------|--------|
| LandingPage | **None** | HIGH GAP |
| LeaderboardPage | **None** | MEDIUM GAP |
| AboutPage | **None** | MEDIUM GAP |
| All other 14 pages | Covered | OK |

## Completely Untested Endpoints

1. `GET /api/auth/leaderboard`
2. `POST /api/lessons/generate-next`
3. `GET /api/admin/audit-logs`

## Top 5 Missing Tests (Priority Order)

1. **AdminService unit tests** — FK cascade, createUser, resetPassword, toggleActive
2. **UserService registration/profile unit tests** — register, verifyEmail, updateProfile, forgotPassword
3. **CertificateService unit tests** — PDF generation, public verification, download
4. **PerformanceService unit tests** — save + history edge cases
5. **LandingPage + LeaderboardPage E2E** — first-visitor experience + competitive feature
