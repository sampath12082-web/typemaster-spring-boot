---
name: run-typemaster
description: Build, run, and drive the TypeMaster app. Use when asked to run, start, test, or verify the app.
---

# Run: TypeMaster

Spring Boot 3.2.5 + React 18 + Vite typing tutor. Backend on port 8081, frontend dev server on port 5174.

## Prerequisites

- Java 21, PostgreSQL 18, Node.js 18+, Maven (wrapper included)
- Local PostgreSQL database `typingtutor` with user `postgres`

## Database Setup

```sql
CREATE DATABASE typingtutor;
```

Tables auto-created by Hibernate `ddl-auto=update`. Seed data loaded from `data-prod.sql` on startup.

## Local Properties

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# Fill in DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET
```

## Run Backend

```bash
# Set JWT_SECRET env var first
export JWT_SECRET="your-32-plus-char-base64-secret"

# Using Maven wrapper (find the right one)
"C:\Users\sampat\.m2\wrapper\dists\apache-maven-3.9.16\56ba1f9f\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=local

# Backend starts at http://localhost:8081 (port set in application-local.properties)
```

## Run Frontend

```bash
cd ../typemaster-ui
npm install
npm run dev
# Frontend starts at http://localhost:5174, proxies /api/* to :8081
```

## Run Tests

### Backend unit tests
```bash
"C:\Users\sampat\.m2\wrapper\dists\apache-maven-3.9.16\56ba1f9f\bin\mvn.cmd" test -Dtest='!com.typingtutor.service.OtpServiceIntegrationTest'
```

### Frontend E2E tests (requires backend running)
```bash
cd ../typemaster-ui/e2e && npx playwright test
```

## Authenticate

```bash
# Admin login (password must be RSA-encrypted for API calls)
# Use the E2E helper functions in e2e/helpers/api.js
# Or login via browser at http://localhost:5174/login
# Default admin: username=admin, password=Admin@123
```

## Key API Endpoints

```bash
# Public
GET  /api/auth/public-key          # RSA public key for password encryption

# Auth (JWT required)
GET  /api/auth/me                  # User profile + stats
GET  /api/auth/ranking             # User's rank and percentile
GET  /api/auth/leaderboard         # Global leaderboard

# Lessons
GET  /api/lessons                  # All lessons with user progress
POST /api/performance              # Submit lesson result

# Exams
POST /api/exams/{tier}/submit      # Submit exam result

# Admin
GET  /api/admin/users              # All users
GET  /api/admin/inquiries          # All support tickets
```

## Environment Variables

| Variable | Required | Purpose |
|----------|----------|---------|
| JWT_SECRET | Yes | JWT signing secret (32+ chars) |
| DB_URL | No | Default: jdbc:postgresql://localhost:5432/typingtutor |
| DB_USERNAME | No | Default: postgres |
| DB_PASSWORD | Yes | PostgreSQL password |
| MAIL_PASSWORD | No | Gmail App Password for OTP emails |
| AI_API_KEY | No | Anthropic API key for AI lesson generation |

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Port 8081 in use | Kill: `Get-NetTCPConnection -LocalPort 8081 \| Stop-Process` |
| DB connection failed | Check PostgreSQL is running: `pg_isready` |
| JWT error on startup | Set JWT_SECRET env var (32+ chars) |
| SCRAM auth error (Java 26) | PostgreSQL driver upgraded to 42.7.7 — rebuild |
| Frontend can't reach backend | Check vite.config.js proxy points to :8081 |
| Admin login fails | Reset via psql: generate BCrypt hash, update app_users |

## Stop

```powershell
Get-NetTCPConnection -LocalPort 8081 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```
