-- V1 baseline: idempotent DDL matching the JPA entities.
-- CREATE TABLE IF NOT EXISTS is safe to run against an existing database
-- (Hibernate ddl-auto=update may have already created these tables).
-- Future schema changes belong in V2__*.sql, V3__*.sql, etc.

-- ── Users ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS app_users (
    id                           BIGSERIAL PRIMARY KEY,
    username                     VARCHAR(255) NOT NULL UNIQUE,
    user_password                VARCHAR(255) NOT NULL,
    role                         VARCHAR(20)  NOT NULL DEFAULT 'USER',
    email                        VARCHAR(255),
    full_name                    VARCHAR(100) NOT NULL DEFAULT '',
    date_of_birth                DATE,
    is_student                   BOOLEAN      NOT NULL DEFAULT FALSE,
    school_name                  VARCHAR(200),
    class_year                   VARCHAR(50),
    course_specialization        VARCHAR(200),
    occupation                   VARCHAR(200),
    placement_completed          BOOLEAN      NOT NULL DEFAULT FALSE,
    recommended_tier             VARCHAR(20),
    placement_wpm                INTEGER,
    is_active                    BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified               BOOLEAN      NOT NULL DEFAULT FALSE,
    password_changed             BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verification_deadline  TIMESTAMP
);

-- ── Lessons ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS lessons (
    id                    BIGSERIAL PRIMARY KEY,
    title                 VARCHAR(255) NOT NULL,
    difficulty_level      VARCHAR(20)  NOT NULL,
    content_text          VARCHAR(2000) NOT NULL,
    display_order         INTEGER,
    min_wpm               INTEGER      NOT NULL DEFAULT 20,
    min_accuracy          DOUBLE PRECISION NOT NULL DEFAULT 85.0,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    is_ai_generated       BOOLEAN      NOT NULL DEFAULT FALSE,
    generated_for_user_id BIGINT
);

-- ── User performance (lesson attempts) ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS user_performance (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT           NOT NULL REFERENCES app_users(id),
    lesson_id           BIGINT           NOT NULL REFERENCES lessons(id),
    wpm                 INTEGER          NOT NULL,
    accuracy_percentage DOUBLE PRECISION NOT NULL,
    completed_at        TIMESTAMP
);

-- ── Email OTP verification ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS email_verifications (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES app_users(id),
    otp_code      VARCHAR(6)   NOT NULL,
    purpose       VARCHAR(20)  NOT NULL,
    expires_at    TIMESTAMP    NOT NULL,
    used          BOOLEAN      NOT NULL DEFAULT FALSE,
    attempt_count INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL
);

-- ── Exams ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS exams (
    id               BIGSERIAL PRIMARY KEY,
    difficulty_level VARCHAR(20)      NOT NULL,
    duration_minutes INTEGER          NOT NULL,
    min_wpm          INTEGER          NOT NULL,
    min_accuracy     DOUBLE PRECISION NOT NULL,
    content_text     TEXT             NOT NULL,
    is_active        BOOLEAN          NOT NULL DEFAULT TRUE
);

-- ── Exam attempts ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS exam_attempts (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT           NOT NULL REFERENCES app_users(id),
    exam_id      BIGINT           NOT NULL REFERENCES exams(id),
    wpm          INTEGER          NOT NULL,
    accuracy     DOUBLE PRECISION NOT NULL,
    passed       BOOLEAN          NOT NULL,
    started_at   TIMESTAMP        NOT NULL,
    completed_at TIMESTAMP        NOT NULL
);

-- ── Certificates ──────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS certificates (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT      NOT NULL REFERENCES app_users(id),
    exam_attempt_id  BIGINT      NOT NULL REFERENCES exam_attempts(id),
    difficulty_level VARCHAR(20) NOT NULL,
    issued_at        TIMESTAMP   NOT NULL,
    certificate_id   VARCHAR(36) NOT NULL UNIQUE,
    pdf_data         BYTEA
);

-- ── Support inquiries ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inquiries (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES app_users(id),
    subject         VARCHAR(255)  NOT NULL,
    message         VARCHAR(2000) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    created_at      TIMESTAMP     NOT NULL,
    admin_response  TEXT,
    reopen_count    INTEGER       NOT NULL DEFAULT 0,
    reopen_reason   VARCHAR(500),
    last_reopened_at TIMESTAMP
);

-- ── Audit log ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    action     VARCHAR(80)  NOT NULL,
    details    VARCHAR(500),
    created_at TIMESTAMP
);
