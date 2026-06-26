# Technical Code Review — TypeMaster
### Date: 2026-06-27

---

## Critical (must fix)

1. **`PerformanceService.savePerformance()` missing `@Transactional`** — writes to DB + audit log without transaction boundary (`PerformanceService.java:41`)
2. **`InquiryService.submitInquiry()` missing `@Transactional`** — writes Inquiry + audit log (`InquiryService.java:36`)
3. **`AdminService.resolveInquiry()` missing `@Transactional`** — updates Inquiry + audit log (`AdminService.java:185`)
4. **`AdminService.getAllInquiries()` missing `@Transactional(readOnly=true)`** — accesses lazy `inq.getUser()`, risks `LazyInitializationException` (`AdminService.java:179`)
5. **`ExamService.getMyExamStatuses()` uses writable `@Transactional`** — should be `readOnly=true` (`ExamService.java:159`)
6. **`CertificateService` read methods use writable `@Transactional`** — `getUserCertificates()` and `getCertificateByPublicId()` should be `readOnly=true` (`CertificateService.java:67,73`)

## Warnings (should fix)

7. **7 more methods missing `@Transactional(readOnly=true)`** — `PerformanceService.getUserHistory()`, `UserService.getRanking()`, `ExamService.getExam()`, `AdminService.getAllUsers()`
8. **5 frontend components missing dark mode** — `TypingEngine.jsx`, `LessonSummaryModal.jsx`, `PasswordStrength.jsx`, `Tooltip.jsx`, `ErrorBoundary.jsx` have 0 `dark:` classes
9. **3 `console.error` calls in production pages** — `DashboardPage:289`, `AnalyticsPage:195`, `LessonPage:42`
10. **`ddl-auto=update` in production** — `application.properties:7` — risky for data integrity
11. **Certificate PDF endpoint auth inconsistency** — metadata is public but PDF requires auth

## Suggestions (nice to have)

12. No `@Cacheable` anywhere — candidates: placement test config, lesson list, leaderboard
13. `/me` makes 5 DB calls per page load — consider single optimized query
14. `AuthController.me()` builds 15+ field map inline — should use a DTO
15. AI lesson generation hardcodes `minWpm=50` — should read from config properties

## Passed

- No `System.out.println` in production code
- No `console.log` in frontend
- No hardcoded secrets
- No sensitive data in logs
- No `@Autowired` field injection
- Controllers are thin — all delegate to services
- Exception convention followed consistently
- SecurityConfig permitAll paths all intentional
- FK cascade order documented and correctly followed
- No N+1 query patterns (pre-loaded via JOIN FETCH)
- All useState hooks called before early returns
- 22/27 component files have dark mode support
