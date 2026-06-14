# TypeMaster — Security Audit

_Audited: 2026-06-12 | Scope: Frontend (React 18) + Backend (Spring Boot 3.2.5)_  
_Last status update: 2026-06-12_

> **Status key:** ⏳ Pending · 🔄 In Progress · ✅ Done · 🚫 Won't Fix · ⬇️ Deferred

---

## Status Dashboard

| Severity | Total | Done | Pending |
|----------|-------|------|---------|
| 🔴 Critical | 5 | 0 | 5 |
| 🟠 High | 5 | 0 | 5 |
| 🟡 Medium | 8 | 0 | 8 |
| 🔵 Low | 5 | 0 | 5 |
| **Total** | **23** | **0** | **23** |

---

## 🔴 Critical

| ID | Status | Finding | File | Fix Effort |
|----|--------|---------|------|-----------|
| SC-1 | ⏳ Pending | JWT secret hardcoded in source — `app.jwt.secret=PPp27x...`. Anyone with repo access can forge JWTs for any user including admins. Move to `${JWT_SECRET}` env var only. | `application.properties:14` | 30 min |
| SC-2 | ⏳ Pending | H2 console accessible without auth — `permitAll()` + `frameOptions().disable()` unconditional. Gate on `@Profile("dev")`; set `spring.h2.console.enabled=false` in production. | `SecurityConfig.java:50,55` | 1 h |
| SC-3 | ⏳ Pending | Temporary passwords shown in clear text in toast — visible to screen capture, shoulder surfing, browser extensions. Replace with "Password reset — share securely." | `AdminPage.jsx:65,91` | 15 min |
| SC-4 | ⏳ Pending | No OTP brute-force rate limiting — 900,000 six-digit combinations enumerable within 30-min validity window. Add `attempt_count` + lockout after 5 failures. | `OtpService.java:57-65` | 2-3 h |
| SC-5 | ⏳ Pending | Default password `Pass@123` pre-filled in Create User form — predictable, visible in React DevTools. Set `password: ''` + non-empty validation before submit. | `AdminPage.jsx:44` | 15 min |

### SC-1 — JWT Secret Fix
```properties
# application.properties
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}
```
Add startup validation that fails if `JWT_SECRET` is empty or shorter than 32 chars.

### SC-2 — H2 Console Fix
```java
@Profile("dev")
@Configuration
class DevSecurityAddons {
    // Move h2-console permitAll() and frameOptions().disable() here
}
```

### SC-3 — Password Toast Fix
```js
// AdminPage.jsx — after createUser
flash(true, `User "${newUser.username}" created. Communicate the password securely.`)
// After resetPassword
flash(true, `Password for "${u.username}" has been reset. Share it through a secure channel.`)
```

### SC-4 — OTP Rate Limiting Fix
Add `attemptCount` field to `EmailVerification` entity. In `OtpService.validateOtp()`:
```java
if (ev.getAttemptCount() >= 5) {
    throw new TooManyAttemptsException("OTP locked. Request a new code.");
}
ev.setAttemptCount(ev.getAttemptCount() + 1);
emailVerificationRepository.save(ev);
```

### SC-5 — Default Password Fix
```js
const [newUser, setNewUser] = useState({ username: '', email: '', password: '' })
// In handleCreate:
if (!newUser.password.trim()) { flash(false, 'Password is required.'); return }
```

---

## 🟠 High

| ID | Status | Finding | File | Fix Effort |
|----|--------|---------|------|-----------|
| SH-1 | ⏳ Pending | JwtAuthFilter silently swallows all exceptions — tampered/expired tokens fall through as unauthenticated instead of rejected with 401. | `JwtAuthFilter.java:60` | 30 min |
| SH-2 | ⏳ Pending | Deactivated users can still authenticate — `isEnabled()` and `isAccountNonLocked()` always return `true`, ignoring `user.isActive()` flag. | `UserPrincipal.java:34-37` | 5 min |
| SH-3 | ⏳ Pending | OTP code logged at DEBUG level in plaintext — if DEBUG enabled in production, OTPs appear in log files. Remove `code=` from log statement. | `OtpService.java:53` | 5 min |
| SH-4 | ⏳ Pending | HTML injection in email templates — `userName` embedded raw into HTML body. Escape with `HtmlUtils.htmlEscape()` before interpolation. | `EmailService.java:42,105` | 15 min |
| SH-5 | ⏳ Pending | Exam results fully client-supplied — WPM/accuracy/timeTaken accepted with no server-side bounds. Add `@Valid` + `@Min`/`@Max` on `ExamSubmitRequest` DTO. | `ExamController.java`, `ExamService.java` | 30 min |

### SH-1 — JWT Filter Fix
```java
} catch (JwtException e) {
    log.warn("[JWT] Rejected invalid token: {}", e.getMessage());
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
    return;
} catch (Exception e) {
    log.error("[JWT] Unexpected error processing token", e);
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    return;
}
```

### SH-2 — UserPrincipal Fix
```java
public boolean isEnabled()          { return user.isActive(); }
public boolean isAccountNonLocked() { return user.isActive(); }
```

### SH-4 — Email HTML Escape Fix
```java
import org.springframework.web.util.HtmlUtils;
// In email template strings:
"<h2>Welcome, " + HtmlUtils.htmlEscape(userName) + "!</h2>"
```

### SH-5 — Exam DTO Validation
```java
public class ExamSubmitRequest {
    @Min(0) @Max(300)    private int wpm;
    @DecimalMin("0.0") @DecimalMax("100.0")  private double accuracy;
    @Min(0)              private int timeTaken;
}
// ExamController: add @Valid to the @RequestBody parameter
```

---

## 🟡 Medium

| ID | Status | Finding | File | Fix Effort |
|----|--------|---------|------|-----------|
| SM-1 | ⏳ Pending | CORS allowed origins hardcoded (`localhost:5173`, `localhost:3000`). Externalise to `${CORS_ALLOWED_ORIGINS}` property. | `SecurityConfig.java:63` | 20 min |
| SM-2 | ⏳ Pending | CORS allowed headers wildcard `*` — overly permissive. Whitelist `Authorization`, `Content-Type`, `X-Requested-With`. | `SecurityConfig.java:65` | 10 min |
| SM-3 | ⏳ Pending | JWT in `localStorage` — accessible to any JavaScript, XSS-stealable. Mitigated by React output escaping; for production prefer `HttpOnly` cookies. | `api.js:9` | 3-5 h |
| SM-4 | ⏳ Pending | Logout is client-side only — token remains cryptographically valid up to 24 h. Consider short-lived tokens + refresh, or server-side blacklist. | `api.js:22-24` | 4-8 h |
| SM-5 | ⏳ Pending | `devOtp` forwarded through router state — visible in React DevTools, browser history. Guard with `import.meta.env.DEV`. | `LoginPage.jsx:40`, `RegisterPage.jsx:70` | 10 min |
| SM-6 | ⏳ Pending | Email change without re-verification — user can swap to any unregistered email for password reset OTPs. Set `emailVerified=false` + send OTP on change. | `UserService.java:227-229` | 2 h |
| SM-7 | ⏳ Pending | Public certificate endpoint returns full payload without auth — enables enumeration/information disclosure. Return minimal `{ valid, tier, issuedAt, username }` for public endpoint. | `CertificateService.java` | 30 min |
| SM-8 | ⏳ Pending | Floating-point comparison for pass/fail — `84.9999 >= 85.0` is false due to IEEE-754. Add epsilon: `accuracy >= minAccuracy - 0.005`. | `ExamService.java:106` | 5 min |

---

## 🔵 Low

| ID | Status | Finding | File | Fix Effort |
|----|--------|---------|------|-----------|
| SL-1 | ⏳ Pending | JWT parsed twice per request in `isTokenValid`. Cache `Claims` object to avoid double parse. | `JwtUtil.java:77` | 20 min |
| SL-2 | ⏳ Pending | `createdAt` set at Java construction time — use `@CreationTimestamp` so DB controls the authoritative timestamp. | `Inquiry.java:29`, `EmailVerification.java:32`, `Certificate.java:27` | 15 min |
| SL-3 | ⏳ Pending | URL `tier` param not validated on frontend — invalid tier sends bad request to backend, shows blank UI. Validate against enum values and redirect on mismatch. | `ExamPage.jsx:13` | 20 min |
| SL-4 | ⏳ Pending | N+1 inquiry deletes in `deleteUser` — one DELETE per inquiry row. Add `deleteAllByUserId(Long)` bulk query to `InquiryRepository`. | `AdminService.java:96-97` | 15 min |
| SL-5 | ⏳ Pending | Admin BCrypt hash committed to source — rotate after first deploy, load initial hash from env var. | `data.sql:51` | 30 min |

---

## Fix Priority Order

| Priority | ID | Severity | Item | Effort |
|----------|----|----------|------|--------|
| 1 | SC-1 | 🔴 Critical | JWT secret → env var | 30 min |
| 2 | SC-3 | 🔴 Critical | Remove passwords from toast | 15 min |
| 3 | SC-5 | 🔴 Critical | Default password removed from form | 15 min |
| 4 | SH-1 | 🟠 High | JWT filter error handling | 30 min |
| 5 | SH-2 | 🟠 High | UserPrincipal.isEnabled() checks active | 5 min |
| 6 | SH-3 | 🟠 High | Remove OTP from debug log | 5 min |
| 7 | SC-4 | 🔴 Critical | OTP rate limiting | 2-3 h |
| 8 | SC-2 | 🔴 Critical | H2 console dev-only | 1 h |
| 9 | SH-4 | 🟠 High | HTML escape email content | 15 min |
| 10 | SH-5 | 🟠 High | Exam DTO bounds validation | 30 min |
| 11 | SM-6 | 🟡 Medium | Email re-verification on change | 2 h |
| 12 | SM-1/SM-2 | 🟡 Medium | CORS configuration | 30 min |
| 13 | SM-5 | 🟡 Medium | devOtp guarded by DEV flag | 10 min |
| 14 | SM-8 | 🟡 Medium | Floating-point epsilon | 5 min |
