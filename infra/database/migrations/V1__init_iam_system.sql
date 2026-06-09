-- ----------------------------------------------------------------------------
-- Table: users
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code           VARCHAR(256) UNIQUE,
    username       VARCHAR(256) UNIQUE NOT NULL,
    email          VARCHAR(256) UNIQUE,
    phone          VARCHAR(30) UNIQUE,
    full_name      VARCHAR(256) NOT NULL,
    given_name     VARCHAR(256),
    family_name    VARCHAR(256),
    avatar_url     VARCHAR(1024),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at  TIMESTAMPTZ,
    deleted_at     TIMESTAMPTZ,
    deleted_by     UUID,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Table: user_credentials
-- ----------------------------------------------------------------------------
CREATE TABLE user_credentials (
    user_id             UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash       VARCHAR(255),
    password_changed_at TIMESTAMPTZ,
    failed_login_count  INTEGER NOT NULL DEFAULT 0,
    locked_until        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Table: user_identities
-- ----------------------------------------------------------------------------
CREATE TABLE user_identities (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider         VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(512) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_user_identities_provider UNIQUE (provider, provider_user_id)
);

CREATE INDEX idx_user_identities_user_id ON user_identities(user_id);

-- ----------------------------------------------------------------------------
-- Table: roles
-- ----------------------------------------------------------------------------
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(256) UNIQUE NOT NULL,
    name        VARCHAR(256) NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  UUID,
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- Table: permissions
-- ----------------------------------------------------------------------------
CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    namespace   VARCHAR(256) NOT NULL DEFAULT 'system',
    resource    VARCHAR(256) NOT NULL,
    code        VARCHAR(256) NOT NULL,
    name        VARCHAR(256) NOT NULL,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  UUID,
    created_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    updated_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_permissions_business ON permissions(namespace, resource, code);

-- ----------------------------------------------------------------------------
-- Table: role_permissions
-- ----------------------------------------------------------------------------
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- ----------------------------------------------------------------------------
-- Table: user_roles
-- ----------------------------------------------------------------------------
CREATE TABLE user_roles (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id     UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    assigned_by UUID REFERENCES users(id) ON DELETE SET NULL,
    revoked_at  TIMESTAMPTZ,
    revoked_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- ----------------------------------------------------------------------------
-- Table: refresh_tokens
-- ----------------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    device_id  VARCHAR(255),
    expired_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expired ON refresh_tokens(expired_at);
