-- =============================================================
-- PostgreSQL Database Initialization
-- Run as: sudo -u postgres psql -f init-db.sql
-- =============================================================

-- Create the database
SELECT 'CREATE DATABASE typemaster'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'typemaster') \gexec

-- Create the application user
-- NOTE: Change the password to match what's in /etc/typemaster/typemaster.env
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'typemaster') THEN
        CREATE USER typemaster WITH PASSWORD 'CHANGE_ME_MATCH_ENV_FILE';
    END IF;
END
$$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE typemaster TO typemaster;

-- Connect to the typemaster database and set schema ownership
\c typemaster

-- Grant schema privileges (needed for Hibernate ddl-auto=update)
GRANT ALL ON SCHEMA public TO typemaster;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO typemaster;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO typemaster;

\echo 'Database typemaster and user typemaster created successfully.'
