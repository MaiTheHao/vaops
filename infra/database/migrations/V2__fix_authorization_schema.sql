-- ----------------------------------------------------------------------------
-- Migration: V2__fix_authorization_schema.sql
-- Description: Refactor authorization tables to Hard Delete & Clean Join Tables
-- ----------------------------------------------------------------------------

-- 1. Refactor user_roles table: Remove soft-delete tracking columns
ALTER TABLE user_roles DROP COLUMN IF EXISTS revoked_at;
ALTER TABLE user_roles DROP COLUMN IF EXISTS revoked_by;

-- 2. Refactor roles table: Remove soft-delete columns
ALTER TABLE roles DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE roles DROP COLUMN IF EXISTS deleted_by;

-- 3. Refactor permissions table: Remove soft-delete columns
ALTER TABLE permissions DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE permissions DROP COLUMN IF EXISTS deleted_by;

-- 4. Ensure standard unique constraints
ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_code_key;
ALTER TABLE roles ADD CONSTRAINT uk_roles_code UNIQUE (code);

DROP INDEX IF EXISTS uk_permissions_action;
CREATE UNIQUE INDEX uk_permissions_action ON permissions (resource, action);
