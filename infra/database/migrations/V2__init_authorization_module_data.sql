-- ============================================================
-- V2__init_authorization_module_data.sql
-- Seed initial authorization data (Roles, Permissions, Role-Permissions)
-- ============================================================

-- -----------------------------------------------------------
-- 1. Seed Roles
-- -----------------------------------------------------------
INSERT INTO roles (id, code, description, is_active, version, created_at, updated_at)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Super Administrator with unrestricted access', TRUE, 0, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000002', 'ADMIN', 'System Administrator with management access', TRUE, 0, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000003', 'USER', 'Standard User with basic privileges', TRUE, 0, NOW(), NOW())
ON CONFLICT (code) DO UPDATE
SET description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- -----------------------------------------------------------
-- 2. Seed Permissions
-- -----------------------------------------------------------
-- User management permissions
INSERT INTO permissions (id, resource, action, description, is_active, created_at, updated_at)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'USER', 'READ', 'Read user information', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000002', 'USER', 'CREATE', 'Create user accounts', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000003', 'USER', 'UPDATE', 'Update user details', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000004', 'USER', 'DELETE', 'Delete user accounts', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000005', 'USER', 'MANAGE_ROLE', 'Assign or revoke roles from users', TRUE, NOW(), NOW())
ON CONFLICT (resource, action) DO UPDATE
SET description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Role management permissions
INSERT INTO permissions (id, resource, action, description, is_active, created_at, updated_at)
VALUES
    ('b0000000-0000-0000-0000-000000000011', 'ROLE', 'READ', 'Read role information', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000012', 'ROLE', 'CREATE', 'Create new roles', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000013', 'ROLE', 'UPDATE', 'Update role details', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000014', 'ROLE', 'DELETE', 'Delete roles', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000015', 'ROLE', 'MANAGE_PERMISSION', 'Assign or revoke permissions from roles', TRUE, NOW(), NOW())
ON CONFLICT (resource, action) DO UPDATE
SET description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Permission management permissions
INSERT INTO permissions (id, resource, action, description, is_active, created_at, updated_at)
VALUES
    ('b0000000-0000-0000-0000-000000000021', 'PERMISSION', 'READ', 'Read permission information', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000022', 'PERMISSION', 'CREATE', 'Create permissions', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000023', 'PERMISSION', 'UPDATE', 'Update permission details', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000024', 'PERMISSION', 'DELETE', 'Delete permissions', TRUE, NOW(), NOW())
ON CONFLICT (resource, action) DO UPDATE
SET description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- Profile permissions
INSERT INTO permissions (id, resource, action, description, is_active, created_at, updated_at)
VALUES
    ('b0000000-0000-0000-0000-000000000031', 'PROFILE', 'READ', 'Read own user profile', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000032', 'PROFILE', 'UPDATE', 'Update own user profile', TRUE, NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000033', 'PROFILE', 'DELETE', 'Delete own user profile', TRUE, NOW(), NOW())
ON CONFLICT (resource, action) DO UPDATE
SET description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();

-- -----------------------------------------------------------
-- 3. Seed Role-Permissions Mapping (role_permissions)
-- -----------------------------------------------------------
-- SUPER_ADMIN & ADMIN: All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code IN ('SUPER_ADMIN', 'ADMIN')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- USER: Profile read/update and basic user read permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON (p.resource = 'PROFILE' OR (p.resource = 'USER' AND p.action = 'READ'))
WHERE r.code = 'USER'
ON CONFLICT (role_id, permission_id) DO NOTHING;
