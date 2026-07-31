-- ============================================================
-- V1__init_schema.sql
-- Initial database schema for VAOPS
-- Generated from domain entities after refactoring to shared base classes
-- ============================================================

-- -----------------------------------------------------------
-- 1. users — Identity aggregate, extends BaseSoftDeletableEntity
-- -----------------------------------------------------------
CREATE TABLE users (
    id              UUID        PRIMARY KEY,
    account_name    VARCHAR(256) NOT NULL,
    password_hash   VARCHAR(256) NOT NULL,
    display_name    VARCHAR(256),
    avatar_url      VARCHAR(1024),
    failed_login_count INTEGER  NOT NULL DEFAULT 0,
    locked_until    TIMESTAMP WITHOUT TIME ZONE,
    last_login_at   TIMESTAMP WITHOUT TIME ZONE,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    deleted_by      UUID,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by      UUID,
    updated_by      UUID,
    CONSTRAINT uq_users_account_name UNIQUE (account_name)
);

-- -----------------------------------------------------------
-- 2. permissions — Authorization aggregate, extends BaseSoftDeletableEntity
-- -----------------------------------------------------------
CREATE TABLE permissions (
    id              UUID        PRIMARY KEY,
    resource        VARCHAR(256) NOT NULL,
    action          VARCHAR(256) NOT NULL,
    description     VARCHAR(1024),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    deleted_by      UUID,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by      UUID,
    updated_by      UUID
);

-- -----------------------------------------------------------
-- 3. roles — Authorization aggregate, extends BaseVersionedEntity
-- -----------------------------------------------------------
CREATE TABLE roles (
    id              UUID        PRIMARY KEY,
    code            VARCHAR(256) NOT NULL,
    description     VARCHAR(1024),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMP WITHOUT TIME ZONE,
    deleted_by      UUID,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER     NOT NULL DEFAULT 0
);

-- -----------------------------------------------------------
-- 4. refresh_tokens — Authentication aggregate, extends BaseEntity
-- -----------------------------------------------------------
CREATE TABLE refresh_tokens (
    id              UUID        PRIMARY KEY,
    user_id         UUID        NOT NULL,
    token_hash      VARCHAR(256) NOT NULL,
    expired_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked_at      TIMESTAMP WITHOUT TIME ZONE,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- -----------------------------------------------------------
-- 5. role_permissions — Join table (Role <-> Permission)
-- -----------------------------------------------------------
CREATE TABLE role_permissions (
    role_id         UUID NOT NULL,
    permission_id   UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id)
        REFERENCES permissions (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------
-- 6. user_roles — Join table (User <-> Role)
-- -----------------------------------------------------------
CREATE TABLE user_roles (
    user_id         UUID NOT NULL,
    role_id         UUID NOT NULL,
    assigned_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    assigned_by     UUID,
    revoked_at      TIMESTAMP WITHOUT TIME ZONE,
    revoked_by      UUID,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
        REFERENCES roles (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------
-- Indexes for performance
-- -----------------------------------------------------------
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expired_at ON refresh_tokens (expired_at);
CREATE INDEX idx_permissions_resource_action ON permissions (resource, action);
CREATE INDEX idx_roles_code ON roles (code);
CREATE INDEX idx_users_account_name ON users (account_name);
