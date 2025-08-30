-- V18: Extend users table for authentication
-- Note: keep nullable to avoid breaking existing seed data; enforce at application level

-- Drop old unique index if exists (case-sensitive)
DROP INDEX IF EXISTS users_email_unique;

-- Add new columns
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS full_name     VARCHAR(255),
  ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
  ADD COLUMN IF NOT EXISTS phone         VARCHAR(50),
  ADD COLUMN IF NOT EXISTS email_lower   VARCHAR(255);

-- Create case-insensitive unique index on lower(email)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes WHERE schemaname = 'public' AND indexname = 'users_email_ci_unique'
  ) THEN
  EXECUTE 'CREATE UNIQUE INDEX users_email_lower_unique ON users (email_lower)';
  END IF;
END $$;
