# TypeMaster — Touch Typing Tutor

A full-stack touch typing web app with structured lessons, real-time WPM/accuracy stats, and user progress tracking.

**Stack:** React 18 + Vite + Tailwind CSS  ·  Spring Boot 3 + Spring Security (JWT)  ·  H2 In-Memory DB + JPA

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17+ |
| Maven | 3.8+ (or use bundled `mvnw`) |
| Node.js | 18+ |
| npm | 9+ |

---

## Project Structure

```
typing-tutor/
├── backend/                  ← Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/typingtutor/
│       ├── entity/           ← JPA entities (User, Lesson, UserPerformance)
│       ├── repository/       ← Spring Data JPA repositories
│       ├── service/          ← Business logic
│       ├── controller/       ← REST controllers
│       ├── security/         ← JWT filter, UserDetailsService
│       ├── config/           ← SecurityConfig (CORS, JWT)
│       └── dto/              ← Request/Response DTOs
│
└── frontend/                 ← React + Vite application
    └── src/
        ├── pages/            ← LoginPage, RegisterPage, DashboardPage, LessonPage
        ├── components/       ← TypingEngine, LessonCard, StatsBar, Modal, Navbar
        ├── hooks/            ← useTyping (typing engine logic)
        ├── context/          ← AuthContext (JWT state)
        └── services/         ← axios API layer
```

---

## Quick Start

### Step 1 — Backend

```bash
cd typing-tutor/backend

# Download Maven wrapper (first time only)
mvn wrapper:wrapper          # or use start-backend.bat / .sh

# Run
./mvnw spring-boot:run       # Linux/Mac
mvnw.cmd spring-boot:run     # Windows
```

Backend starts at **http://localhost:8080**  
H2 Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:typingtutor`, user: `sa`, password: empty)

### Step 2 — Frontend

```bash
cd typing-tutor/frontend
npm install
npm run dev
```

Frontend starts at **http://localhost:5173**

---

## REST API Reference

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register new user |
| POST | `/api/auth/login` | No | Login, returns JWT |
| GET  | `/api/auth/me` | JWT | Get current user stats |

### Lessons
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/lessons` | JWT | All lessons with user progress |
| GET | `/api/lessons/{id}` | JWT | Single lesson detail |

### Performance
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/performance` | JWT | Save a completed lesson result |
| GET  | `/api/performance/history` | JWT | User's full history |

**Auth header:** `Authorization: Bearer <token>`

---

## Database Schema

```sql
users            (id, username, password, created_at)
lessons          (id, title, difficulty_level, content_text, display_order)
user_performance (id, user_id, lesson_id, wpm, accuracy_percentage, completed_at)
```

24 pre-seeded lessons across 3 tiers (8 Basic / 8 Intermediate / 8 Advanced).

---

## Lesson Tiers

| Tier | Focus |
|------|-------|
| 🌱 Basic | Home row keys, finger placement, ASDF JKL; drills |
| 🚀 Intermediate | Full sentences, punctuation, capitalization |
| ⚡ Advanced | Numbers, symbols, code snippets, speed tests |

---

## WPM Calculation

```
WPM = (characters_typed ÷ 5) ÷ elapsed_minutes
```

Accuracy = `(correct_keystrokes / total_keystrokes) × 100`

---

## Deploying to Production

1. Switch H2 to a persistent DB (PostgreSQL/MySQL) in `application.properties`
2. Set `spring.jpa.hibernate.ddl-auto=validate`
3. Set a strong `app.jwt.secret` (32+ byte hex)
4. Build frontend: `npm run build` → serve `dist/` via Nginx or Spring Boot static resources
5. Package backend: `./mvnw package` → run `target/typing-tutor-backend-1.0.0.jar`

---

## Moving to D:\claude\typing-tutor

Copy the entire `typing-tutor/` folder to `D:\claude\typing-tutor\` then run the `.bat` scripts directly.
