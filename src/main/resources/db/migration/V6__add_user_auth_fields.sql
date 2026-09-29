-- V1 already created users with username/email/password_hash, this adds what auth needs on top
-- new signups start disabled, an admin has to enable them
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';