---
name: project-review
description: Project-specific technical review. Use when asked to do a project review, check project quality, audit project security, or inspect the full codebase.
---

# Technical Code Review

Performs a structured code review of changed or specified files. Checks for correctness bugs, security issues, performance problems, and code quality.

## When to Use

- Before committing significant changes
- When asked to "review code", "check for bugs", "audit security"
- After implementing a new feature
- Before merging a PR

## Review Checklist

### Correctness
- [ ] Code does what the enhancement/bug description says
- [ ] Edge cases handled (null, empty, zero, negative)
- [ ] Endpoints return correct HTTP status codes
- [ ] Error messages user-friendly, not leaking internals
- [ ] Services throw IllegalArgumentException (400) or NoSuchElementException (404)

### Security
- [ ] No hardcoded secrets
- [ ] Passwords hashed with BCrypt, encrypted in transit with RSA-OAEP
- [ ] JWT validated on all protected endpoints
- [ ] Admin endpoints check ROLE_ADMIN at filter chain AND @PreAuthorize
- [ ] SQL injection safe (Spring Data JPA parameterized queries)
- [ ] Sensitive data (passwords, OTPs) never in INFO-level logs

### Performance
- [ ] No N+1 query patterns
- [ ] Write methods have @Transactional, reads have @Transactional(readOnly=true)
- [ ] /me endpoint not overloaded (it's called on every page load)
- [ ] No unnecessary findAll() when filtered query exists

### Code Quality
- [ ] Controllers thin — no business logic
- [ ] DTOs used, entities never exposed directly
- [ ] @CreationTimestamp for timestamps, not LocalDateTime.now()
- [ ] SLF4J logging only, no System.out.println
- [ ] No commented-out code blocks

### Database
- [ ] Hibernate ddl-auto=update handles schema changes
- [ ] Delete cascade order respected: certificates → exam_attempts → user_performance → inquiries → app_users
- [ ] Reserved words aliased: user_password, app_users

### Frontend
- [ ] React hooks called before any early return
- [ ] No state mutations during render
- [ ] All API calls through src/services/api.js
- [ ] Loading and error states handled
- [ ] Dark mode variants included for new components

## Report Format

```
## Code Review: <scope>

### Critical (must fix)
### Warnings (should fix)
### Suggestions (nice to have)
### Passed
```
