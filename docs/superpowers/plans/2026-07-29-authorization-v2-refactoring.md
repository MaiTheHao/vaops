# Authorization Module V2 Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the entire Authorization module to use Hard Delete for RBAC, Unidirectional Many-to-Many (`Role` -> `Set<Permission>`), Rich Domain Model with Value Objects & JPA Converters, and a 3+1 Hybrid Services structure based on Aggregate Boundaries.

**Architecture:** 
1. Database Schema (`V2__fix_authorization_schema.sql`): Remove soft delete tracking columns (`revoked_at`, `revoked_by` in `user_roles`; `deleted_at`, `deleted_by` in `roles` & `permissions`).
2. Rich Domain Entities: Remove class-level `@Setter`, add Value Objects with `@Converter(autoApply = true)`, encapsulate logic in domain methods (`Role`, `Permission`, `UserRole`).
3. 3+1 Hybrid Services: Consolidate 17 use cases into 4 services (`RoleService`, `PermissionService`, `UserRoleService`, `CheckPermissionService`) and centralized mappers (`RoleResponseMapper`, `PermissionResponseMapper`).

**Tech Stack:** Java 21, Spring Boot 3, JPA/Hibernate, Flyway, MapStruct / Lombok, Maven, JUnit 5.

## Global Constraints

- Absolute paths must be used for file creation and modification.
- Hard delete policy for RBAC tables (`roles`, `permissions`, `user_roles`).
- Unidirectional Many-to-Many relationship (`Role` -> `Set<Permission>`).
- Encapsulation: No `@Setter` at class level in Entities; expose mutation only via domain methods.
- 3+1 Service Package Structure (`RoleService`, `PermissionService`, `UserRoleService`, `CheckPermissionService`).

---

### Task 1: Flyway DB Schema Migration V2

**Files:**
- Create: `infra/database/migrations/V2__fix_authorization_schema.sql`

**Interfaces:**
- Consumes: PostgreSQL DB schema from `V1__init_iam_system.sql`.
- Produces: Updated SQL schema without soft-delete columns on `roles`, `permissions`, and `user_roles`.

- [ ] **Step 1: Create Flyway Migration V2 SQL script**

```sql
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
```

- [ ] **Step 2: Commit Migration V2 File**

```bash
git add infra/database/migrations/V2__fix_authorization_schema.sql
git commit -m "db(migration): add V2 migration script for authorization hard delete schema"
```

---

### Task 2: Value Objects & Auto-Apply JPA Converters

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/valueobject/RoleCode.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/converter/RoleCodeConverter.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/valueobject/PermissionResource.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/converter/PermissionResourceConverter.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/valueobject/PermissionAction.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/converter/PermissionActionConverter.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/valueobject/PermissionDescription.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/converter/PermissionDescriptionConverter.java`

**Interfaces:**
- Consumes: `ValidationException` from `c4f.vannang.vaops.shared.exception.ValidationException`.
- Produces: Strongly typed Value Objects with JPA `@Converter(autoApply = true)` attributes.

- [ ] **Step 1: Write Value Objects and JPA Converters**

`RoleCode.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record RoleCode(String value) {
  public RoleCode {
    if (value == null || value.isBlank()) {
      throw new ValidationException("Role code must not be null or blank");
    }
    value = value.strip().toUpperCase();
    if (value.length() > 256) {
      throw new ValidationException("Role code must not exceed 256 characters");
    }
  }
}
```

`RoleCodeConverter.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleCodeConverter implements AttributeConverter<RoleCode, String> {
  @Override
  public String convertToDatabaseColumn(RoleCode attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public RoleCode convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new RoleCode(dbData);
  }
}
```

`PermissionResource.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record PermissionResource(String value) {
  public PermissionResource {
    if (value == null || value.isBlank()) {
      throw new ValidationException("Permission resource must not be null or blank");
    }
    value = value.strip().toUpperCase();
    if (value.length() > 256) {
      throw new ValidationException("Permission resource must not exceed 256 characters");
    }
  }
}
```

`PermissionResourceConverter.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PermissionResourceConverter implements AttributeConverter<PermissionResource, String> {
  @Override
  public String convertToDatabaseColumn(PermissionResource attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PermissionResource convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PermissionResource(dbData);
  }
}
```

`PermissionAction.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record PermissionAction(String value) {
  public PermissionAction {
    if (value == null || value.isBlank()) {
      throw new ValidationException("Permission action must not be null or blank");
    }
    value = value.strip().toUpperCase();
    if (value.length() > 256) {
      throw new ValidationException("Permission action must not exceed 256 characters");
    }
  }
}
```

`PermissionActionConverter.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PermissionActionConverter implements AttributeConverter<PermissionAction, String> {
  @Override
  public String convertToDatabaseColumn(PermissionAction attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PermissionAction convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PermissionAction(dbData);
  }
}
```

`PermissionDescription.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record PermissionDescription(String value) {
  public PermissionDescription {
    if (value != null) {
      value = value.strip();
      if (value.length() > 1024) {
        throw new ValidationException("Permission description must not exceed 1024 characters");
      }
    }
  }
}
```

`PermissionDescriptionConverter.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PermissionDescriptionConverter implements AttributeConverter<PermissionDescription, String> {
  @Override
  public String convertToDatabaseColumn(PermissionDescription attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PermissionDescription convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PermissionDescription(dbData);
  }
}
```

- [ ] **Step 2: Commit Value Objects & Converters**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/valueobject/
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/converter/
git commit -m "feat(authorization): add Value Objects and auto-apply JPA converters"
```

---

### Task 3: Refactor Rich Domain Entities (`Permission`, `Role`, `UserRole`)

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Permission.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Role.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/UserRole.java`

**Interfaces:**
- Consumes: Value Objects (`RoleCode`, `PermissionResource`, `PermissionAction`, `PermissionDescription`).
- Produces: Rich Domain Entities without class-level `@Setter`, with domain mutation methods.

- [ ] **Step 1: Refactor `Permission.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "resource", nullable = false, length = 256)
  private PermissionResource resource;

  @Column(name = "action", nullable = false, length = 256)
  private PermissionAction action;

  @Column(name = "description", nullable = true, length = 1024)
  private PermissionDescription description;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by", nullable = true)
  private UUID createdBy;

  @Column(name = "updated_by", nullable = true)
  private UUID updatedBy;

  public void setId(UUID id) {
    this.id = id;
  }

  public static Permission create(
      PermissionResource resource,
      PermissionAction action,
      PermissionDescription description,
      UUID createdBy) {
    Permission p = new Permission();
    p.resource = resource;
    p.action = action;
    p.description = description;
    p.createdBy = createdBy;
    p.active = true;
    return p;
  }

  public void updateInfo(
      PermissionResource resource,
      PermissionAction action,
      PermissionDescription description,
      UUID updatedBy) {
    this.resource = resource;
    this.action = action;
    this.description = description;
    this.updatedBy = updatedBy;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}
```

- [ ] **Step 2: Refactor `Role.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "code", nullable = false, length = 256)
  private RoleCode code;

  @Column(name = "description", nullable = true, length = 1024)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @ManyToMany
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  private Set<Permission> permissions = new HashSet<>();

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by", nullable = true)
  private UUID createdBy;

  @Column(name = "updated_by", nullable = true)
  private UUID updatedBy;

  public void setId(UUID id) {
    this.id = id;
  }

  public static Role create(RoleCode code, String description, UUID createdBy) {
    Role r = new Role();
    r.code = code;
    r.description = description;
    r.createdBy = createdBy;
    r.active = true;
    return r;
  }

  public void updateInfo(RoleCode code, String description, UUID updatedBy) {
    this.code = code;
    this.description = description;
    this.updatedBy = updatedBy;
  }

  public void assignPermission(Permission permission) {
    if (permission != null && permission.isActive()) {
      this.permissions.add(permission);
    }
  }

  public void assignPermissions(Collection<Permission> newPermissions) {
    if (newPermissions != null) {
      newPermissions.stream()
          .filter(Permission::isActive)
          .forEach(this.permissions::add);
    }
  }

  public void revokePermission(Permission permission) {
    if (permission != null) {
      this.permissions.remove(permission);
    }
  }

  public void revokePermissions(Collection<Permission> permissionsToRevoke) {
    if (permissionsToRevoke != null) {
      this.permissions.removeAll(permissionsToRevoke);
    }
  }

  public boolean hasPermission(PermissionResource resource, PermissionAction action) {
    if (!this.active) return false;
    return this.permissions.stream()
        .anyMatch(p -> p.isActive() 
                    && p.getResource().equals(resource) 
                    && p.getAction().equals(action));
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}
```

- [ ] **Step 3: Refactor `UserRole.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

  @EmbeddedId
  private UserRoleId id;

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  @Column(name = "assigned_by")
  private UUID assignedBy;

  public static UserRole assign(UUID userId, UUID roleId, UUID assignedBy) {
    UserRole ur = new UserRole();
    ur.id = new UserRoleId(userId, roleId);
    ur.assignedAt = Instant.now();
    ur.assignedBy = assignedBy;
    return ur;
  }
}
```

- [ ] **Step 4: Commit Rich Domain Entities**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/
git commit -m "refactor(authorization): convert Permission, Role, UserRole to Rich Domain Model"
```

---

### Task 4: Create Shared Response Mappers

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/mapper/PermissionResponseMapper.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/mapper/RoleResponseMapper.java`

**Interfaces:**
- Consumes: Domain Entities (`Permission`, `Role`).
- Produces: DTO Responses (`PermissionResponse`, `RoleResponse`).

- [ ] **Step 1: Write `PermissionResponseMapper.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.mapper;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;

@Component
public class PermissionResponseMapper {

  public PermissionResponse toResponse(Permission permission) {
    if (permission == null) return null;
    return PermissionResponse.builder()
        .id(permission.getId())
        .resource(permission.getResource() != null ? permission.getResource().value() : null)
        .action(permission.getAction() != null ? permission.getAction().value() : null)
        .description(permission.getDescription() != null ? permission.getDescription().value() : null)
        .isActive(permission.isActive())
        .createdAt(permission.getCreatedAt())
        .updatedAt(permission.getUpdatedAt())
        .deletedAt(null)
        .deletedBy(null)
        .createdBy(permission.getCreatedBy())
        .updatedBy(permission.getUpdatedBy())
        .build();
  }
}
```

- [ ] **Step 2: Write `RoleResponseMapper.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleResponseMapper {

  private final PermissionResponseMapper permissionResponseMapper;

  public RoleResponse toResponse(Role role) {
    if (role == null) return null;
    
    Set<PermissionResponse> permissionResponses = role.getPermissions() == null
        ? Collections.emptySet()
        : role.getPermissions().stream()
            .map(permissionResponseMapper::toResponse)
            .collect(Collectors.toSet());

    return RoleResponse.builder()
        .id(role.getId())
        .code(role.getCode() != null ? role.getCode().value() : null)
        .description(role.getDescription())
        .isActive(role.isActive())
        .permissions(permissionResponses)
        .createdAt(role.getCreatedAt())
        .updatedAt(role.getUpdatedAt())
        .deletedAt(null)
        .deletedBy(null)
        .createdBy(role.getCreatedBy())
        .updatedBy(role.getUpdatedBy())
        .build();
  }
}
```

- [ ] **Step 3: Commit Mappers**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/mapper/
git commit -m "feat(authorization): add shared RoleResponseMapper and PermissionResponseMapper"
```

---

### Task 5: Implement 3+1 Hybrid Services (`RoleService`, `PermissionService`, `UserRoleService`, `CheckPermissionService`)

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/PermissionService.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/RoleService.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/UserRoleService.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/CheckPermissionService.java`

**Interfaces:**
- Consumes: Command & Query DTOs, Repositories, Domain Entities, Mappers.
- Produces: Clean Application Services organized by Aggregate Boundaries.

- [ ] **Step 1: Implement `PermissionService.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.mapper.PermissionResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

  private final PermissionQueryRepository permissionQueryRepository;
  private final PermissionWriteRepository permissionWriteRepository;
  private final PermissionResponseMapper permissionResponseMapper;

  public PermissionResponse createPermission(CreatePermissionCommand command) {
    if (command == null) {
      throw new ValidationException("Command must not be null");
    }
    PermissionResource resource = new PermissionResource(command.resource());
    PermissionAction action = new PermissionAction(command.action());
    PermissionDescription description = command.description() != null ? new PermissionDescription(command.description()) : null;

    if (permissionQueryRepository.existsByResourceAndAction(resource.value(), action.value())) {
      throw new ResourceAlreadyExistsException("Permission with resource and action already exists");
    }

    Permission permission = Permission.create(resource, action, description, command.createdBy());
    Permission saved = permissionWriteRepository.save(permission);
    return permissionResponseMapper.toResponse(saved);
  }

  public PermissionResponse updatePermission(UpdatePermissionCommand command) {
    if (command == null || command.id() == null) {
      throw new ValidationException("Command and ID must not be null");
    }
    Permission permission = permissionQueryRepository.findById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

    PermissionResource resource = new PermissionResource(command.resource());
    PermissionAction action = new PermissionAction(command.action());
    PermissionDescription description = command.description() != null ? new PermissionDescription(command.description()) : null;

    permission.updateInfo(resource, action, description, command.updatedBy());
    Permission saved = permissionWriteRepository.save(permission);
    return permissionResponseMapper.toResponse(saved);
  }

  public void deletePermission(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    if (!permissionQueryRepository.findById(id).isPresent()) {
      throw new ResourceNotFoundException("Permission not found");
    }
    permissionWriteRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public PermissionResponse getPermissionById(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    Permission permission = permissionQueryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
    return permissionResponseMapper.toResponse(permission);
  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> listPermissions() {
    return permissionQueryRepository.findAllActive().stream()
        .map(permissionResponseMapper::toResponse)
        .collect(Collectors.toList());
  }
}
```

- [ ] **Step 2: Implement `RoleService.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokePermissionFromRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.mapper.RoleResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

  private final RoleQueryRepository roleQueryRepository;
  private final RoleWriteRepository roleWriteRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final RoleResponseMapper roleResponseMapper;

  public RoleResponse createRole(CreateRoleCommand command) {
    if (command == null) throw new ValidationException("Command must not be null");
    RoleCode code = new RoleCode(command.code());

    if (roleQueryRepository.existsByCode(code.value())) {
      throw new ResourceAlreadyExistsException("Role code already exists");
    }

    Role role = Role.create(code, command.description(), command.createdBy());
    Role saved = roleWriteRepository.save(role);
    return roleResponseMapper.toResponse(saved);
  }

  public RoleResponse updateRole(UpdateRoleCommand command) {
    if (command == null || command.id() == null) throw new ValidationException("Command and ID must not be null");
    Role role = roleQueryRepository.findById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    RoleCode code = new RoleCode(command.code());
    role.updateInfo(code, command.description(), command.updatedBy());
    Role saved = roleWriteRepository.save(role);
    return roleResponseMapper.toResponse(saved);
  }

  public void deleteRole(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    if (!roleQueryRepository.findById(id).isPresent()) {
      throw new ResourceNotFoundException("Role not found");
    }
    roleWriteRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public RoleResponse getRoleById(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    Role role = roleQueryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    return roleResponseMapper.toResponse(role);
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> listRoles() {
    return roleQueryRepository.findAllActive().stream()
        .map(roleResponseMapper::toResponse)
        .collect(Collectors.toList());
  }

  public void assignPermissionsToRole(AssignPermissionToRoleCommand command) {
    if (command == null || command.roleId() == null || command.permissionIds() == null || command.permissionIds().isEmpty()) {
      throw new ValidationException("RoleId and permissionIds must not be empty");
    }

    Role role = roleQueryRepository.findById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    List<Permission> permissions = permissionQueryRepository.findAllActiveByIds(new ArrayList<>(command.permissionIds()));
    if (permissions.size() != command.permissionIds().size()) {
      throw new ResourceNotFoundException("One or more permissions were not found");
    }

    role.assignPermissions(permissions);
    roleWriteRepository.save(role);
  }

  public void revokePermissionsFromRole(RevokePermissionFromRoleCommand command) {
    if (command == null || command.roleId() == null || command.permissionIds() == null || command.permissionIds().isEmpty()) {
      throw new ValidationException("RoleId and permissionIds must not be empty");
    }

    Role role = roleQueryRepository.findById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    List<Permission> permissions = permissionQueryRepository.findAllActiveByIds(new ArrayList<>(command.permissionIds()));
    role.revokePermissions(permissions);
    roleWriteRepository.save(role);
  }
}
```

- [ ] **Step 3: Implement `UserRoleService.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRoleToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.mapper.PermissionResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.mapper.RoleResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

  private final RoleQueryRepository roleQueryRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final UserRoleQueryRepository userRoleQueryRepository;
  private final UserRoleWriteRepository userRoleWriteRepository;
  private final RoleResponseMapper roleResponseMapper;
  private final PermissionResponseMapper permissionResponseMapper;

  public void assignRolesToUser(AssignRoleToUserCommand command) {
    if (command == null || command.userId() == null || command.roleIds() == null || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }

    List<UUID> roleIdList = new ArrayList<>(command.roleIds());
    List<Role> activeRoles = roleQueryRepository.findAllActiveByIds(roleIdList);
    if (activeRoles.size() != command.roleIds().size()) {
      throw new ResourceNotFoundException("One or more roles were not found");
    }

    List<UserRole> toSave = roleIdList.stream()
        .map(roleId -> UserRole.assign(command.userId(), roleId, command.assignedBy()))
        .collect(Collectors.toList());

    userRoleWriteRepository.saveAll(toSave);
  }

  public void revokeRolesFromUser(RevokeRoleFromUserCommand command) {
    if (command == null || command.userId() == null || command.roleIds() == null || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }
    userRoleWriteRepository.deleteByUserIdAndRoleIdIn(command.userId(), command.roleIds());
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> getUserRoles(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    List<Role> roles = userRoleQueryRepository.findActiveRolesByUserId(userId);
    return roles.stream().map(roleResponseMapper::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> getUserPermissions(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    List<Permission> permissions = permissionQueryRepository.findAllActiveByUserId(userId);
    return permissions.stream().map(permissionResponseMapper::toResponse).collect(Collectors.toList());
  }
}
```

- [ ] **Step 4: Implement `CheckPermissionService.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.dto.CheckPermissionQuery;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckPermissionService {

  private final PermissionQueryRepository permissionQueryRepository;

  public boolean checkPermission(CheckPermissionQuery query) {
    if (query == null || query.userId() == null || query.resource() == null || query.action() == null) {
      throw new ValidationException("UserId, resource, and action must not be null");
    }

    PermissionResource resource = new PermissionResource(query.resource());
    PermissionAction action = new PermissionAction(query.action());

    return permissionQueryRepository.hasPermission(query.userId(), resource.value(), action.value());
  }
}
```

- [ ] **Step 5: Commit Services**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/
git commit -m "feat(authorization): implement 3+1 Hybrid Services (RoleService, PermissionService, UserRoleService, CheckPermissionService)"
```

---

### Task 6: Delete Legacy Usecases & Update Repositories

**Files:**
- Delete: All 17 files in `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/`
- Modify: Repositories in `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/` to support hard delete deleteBy operations if required.

- [ ] **Step 1: Delete legacy use case directory**

```bash
rm -rf backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase
```

- [ ] **Step 2: Update `UserRoleWriteRepository.java` to add `deleteByUserIdAndRoleIdIn`**

`UserRoleWriteRepository.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;

public interface UserRoleWriteRepository extends JpaRepository<UserRole, UserRoleId> {
  void deleteByUserIdAndRoleIdIn(UUID userId, Collection<UUID> roleIds);
}
```

- [ ] **Step 3: Commit Legacy Cleanup & Repository updates**

```bash
git rm -r backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/
git commit -m "refactor(authorization): remove 17 legacy usecases and add hard-delete repository methods"
```

---

### Task 7: Maven Compilation & Verification Test

**Files:**
- Verify: Entire `backend` module build and compilation.

- [ ] **Step 1: Run Maven test-compile to verify zero build errors**

Run: `cd backend && ./mvnw test-compile`  
Expected output: `BUILD SUCCESS`

- [ ] **Step 2: Final git status check and commit**

```bash
git status
```

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-07-29-authorization-v2-refactoring.md`. Two execution options:

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
