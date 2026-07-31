-- ============================================================
-- V1__init_iam_system.sql
-- Initial database schema for VAOPS IAM System
-- Generated from domain entities after refactoring to shared base classes
-- ============================================================

-- -----------------------------------------------------------
-- 1. users — Identity aggregate, extends BaseSoftDeletableEntity
-- -----------------------------------------------------------
CREATE TABLE users (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    account_name        VARCHAR(256) NOT NULL,
    password_hash       VARCHAR(256) NOT NULL,
    display_name        VARCHAR(256),
    avatar_url          VARCHAR(1024),
    failed_login_count  INTEGER     NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    last_login_at       TIMESTAMPTZ,
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at          TIMESTAMPTZ,
    deleted_by          UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by          UUID,
    updated_by          UUID,
    CONSTRAINT uq_users_account_name UNIQUE (account_name),
    CONSTRAINT fk_users_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL
);

-- -----------------------------------------------------------
-- 2. permissions — Authorization aggregate, extends BaseSoftDeletableEntity
-- -----------------------------------------------------------
CREATE TABLE permissions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    resource        VARCHAR(256) NOT NULL,
    action          VARCHAR(256) NOT NULL,
    description     VARCHAR(1024),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    deleted_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID,
    CONSTRAINT uq_permissions_resource_action UNIQUE (resource, action),
    CONSTRAINT fk_permissions_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_permissions_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_permissions_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL
);

-- -----------------------------------------------------------
-- 3. roles — Authorization aggregate, extends BaseVersionedEntity
-- -----------------------------------------------------------
CREATE TABLE roles (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(256) NOT NULL,
    description     VARCHAR(1024),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    deleted_by      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT uq_roles_code UNIQUE (code),
    CONSTRAINT fk_roles_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_roles_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_roles_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL
);

-- -----------------------------------------------------------
-- 4. refresh_tokens — Authentication aggregate, extends BaseEntity
-- -----------------------------------------------------------
CREATE TABLE refresh_tokens (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL,
    token_hash      VARCHAR(256) NOT NULL,
    expired_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------
-- 5. role_permissions — Join table (Role <-> Permission)
-- -----------------------------------------------------------
CREATE TABLE role_permissions (
    role_id         UUID NOT NULL,
    permission_id   UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------
-- 6. user_roles — Join table (User <-> Role)
-- -----------------------------------------------------------
CREATE TABLE user_roles (
    user_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by     UUID,
    revoked_at      TIMESTAMPTZ,
    revoked_by      UUID,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_user_roles_revoked_by FOREIGN KEY (revoked_by) REFERENCES users (id) ON DELETE SET NULL
);

-- -----------------------------------------------------------
-- Indexes for performance & query optimization
-- -----------------------------------------------------------
CREATE INDEX idx_users_account_name ON users (account_name);
CREATE INDEX idx_users_id_active_deleted ON users (id, is_active, deleted_at);

CREATE INDEX idx_permissions_resource_action ON permissions (resource, action);
CREATE INDEX idx_permissions_id_active_deleted ON permissions (id, is_active, deleted_at);

CREATE INDEX idx_roles_code ON roles (code);
CREATE INDEX idx_roles_id_active_deleted ON roles (id, is_active, deleted_at);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expired_at ON refresh_tokens (expired_at);
CREATE INDEX idx_refresh_tokens_user_expired ON refresh_tokens (user_id, expired_at);

CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);

CREATE INDEX idx_user_roles_role ON user_roles (role_id);
