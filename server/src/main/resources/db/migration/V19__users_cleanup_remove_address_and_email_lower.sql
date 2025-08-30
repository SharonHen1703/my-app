-- V19: Cleanup users table - remove Address and email_lower; enforce unique lower(email)

-- Normalize emails again for safety
UPDATE public.users SET email = lower(trim(email)) WHERE email IS NOT NULL;

-- Drop address column (ensure not present)
ALTER TABLE public.users DROP COLUMN IF EXISTS address;

-- Drop email_lower artifacts introduced in V18
ALTER TABLE public.users DROP COLUMN IF EXISTS email_lower;
DROP INDEX IF EXISTS users_email_lower_unique;
DROP INDEX IF EXISTS users_email_ci_unique;

-- Ensure case-insensitive unique index on lower(email)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_lower ON public.users (lower(email));
