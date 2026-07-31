# Soft Delete & Activatable Standardization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize query repositories with explicit JPQL filtering out soft-deleted records (`deletedAt IS NULL`), refactor entity active status management via a new `Activatable` domain interface, and support hard-delete maintenance operations via explicit `WithDeleted` queries.

**Architecture:** 
- Extract status management (`active`) out of `BaseSoftDeletableEntity` into a new domain interface `Activatable`.
- `User`, `Role`, and `Permission` implement `Activatable` and maintain their own `is_active` DB mapping.
- All Query Repositories use explicit `@Query` annotations with explicit `deletedAt IS NULL` conditions for Tier 1 standard queries and provide Tier 3 `WithDeleted` queries for hard-delete purge services.

**Tech Stack:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate, JUnit 5

## Global Constraints

- Domain entities in `domain/` use JPA annotations directly.
- All repository queries MUST use explicit JPQL `@Query(...)` strings; do NOT rely on Spring Data derived query method name parsing.
- Preserve existing public API contracts and method signatures.

---

### Task 1: Create `Activatable` Interface and Update `BaseSoftDeletableEntity`

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/base/domain/Activatable.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/shared/base/domain/BaseSoftDeletableEntity.java`

**Interfaces:**
- Consumes: None
- Produces: `Activatable` interface with `boolean isActive()`, updated `BaseSoftDeletableEntity` without `active` field.

- [ ] **Step 1: Create `Activatable.java` interface**

Create file `backend/src/main/java/c4f/vannang/vaops/shared/base/domain/Activatable.java`:
```java
package c4f.vannang.vaops.shared.base.domain;

public interface Activatable {
  boolean isActive();
}
```

- [ ] **Step 2: Remove `active` field and status methods from `BaseSoftDeletableEntity`**

Modify `backend/src/main/java/c4f/vannang/vaops/shared/base/domain/BaseSoftDeletableEntity.java`:
Remove `active` field, `@Column(name = "is_active")`, `activate()`, and `deactivate()` methods.
Update `softDelete`:
```java
public void softDelete(UUID deletedBy) {
  this.deletedAt = LocalDateTime.now();
  this.deletedBy = deletedBy;
}
```

- [ ] **Step 3: Compile to verify BaseSoftDeletableEntity changes**

Run: `./mvnw compile` or `./gradlew compileJava`
Expected: Compilation errors in `User`, `Role`, `Permission` (to be fixed in Task 2).

- [ ] **Step 4: Commit Task 1 scaffolding**

```bash
git add backend/src/main/java/c4f/vannang/vaops/shared/base/domain/Activatable.java backend/src/main/java/c4f/vannang/vaops/shared/base/domain/BaseSoftDeletableEntity.java
git commit -m "refactor(domain): create Activatable interface and strip active field from BaseSoftDeletableEntity"
```

---

### Task 2: Implement `Activatable` in `User`, `Role`, and `Permission` Entities

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/domain/User.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Role.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Permission.java`

**Interfaces:**
- Consumes: `Activatable`
- Produces: Entities implementing `Activatable` with `active` field and `activate()`, `deactivate()`, `isActive()` methods.

- [ ] **Step 1: Update `User.java`**

Add `implements Activatable` to `User`.
Add `active` field and methods:
```java
@Column(name = "is_active", nullable = false)
private boolean active = true;

@Override
public boolean isActive() {
  return active;
}

public void activate() {
  this.active = true;
}

public void deactivate() {
  this.active = false;
}
```

- [ ] **Step 2: Update `Role.java`**

Add `implements Activatable` to `Role`.
Add `active` field and methods:
```java
@Column(name = "is_active", nullable = false)
private boolean active = true;

@Override
public boolean isActive() {
  return active;
}

public void activate() {
  this.active = true;
}

public void deactivate() {
  this.active = false;
}
```

- [ ] **Step 3: Update `Permission.java`**

Add `implements Activatable` to `Permission`.
Add `active` field and methods:
```java
@Column(name = "is_active", nullable = false)
private boolean active = true;

@Override
public boolean isActive() {
  return active;
}

public void activate() {
  this.active = true;
}

public void deactivate() {
  this.active = false;
}
```

- [ ] **Step 4: Verify entity compilation**

Run: `./mvnw test-compile` or `./gradlew testClasses`
Expected: Success or minor test mock fixes.

- [ ] **Step 5: Commit Task 2 changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/domain/User.java backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Role.java backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Permission.java
git commit -m "feat(domain): implement Activatable interface on User, Role, and Permission"
```

---

### Task 3: Standardize Repositories with Explicit JPQL Tiered Queries

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/repository/UserQueryRepository.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/RoleQueryRepository.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java`

**Interfaces:**
- Consumes: `User`, `Role`, `Permission`
- Produces: Tier 1 (standard soft-delete filtered), Tier 2 (active filtered), and Tier 3 (`WithDeleted`) repository query methods.

- [ ] **Step 1: Update `UserQueryRepository.java`**

Add explicit `@Query` annotations with `deletedAt IS NULL` on standard methods and add `findByIdWithDeleted`:
```java
package c4f.vannang.vaops.modules.identity/internal/repository;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserQueryRepository extends BaseQueryRepository<User, UUID> {

  @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
  Optional<User> findById(@Param("id") UUID id);

  @Query("SELECT u FROM User u WHERE u.id = :id")
  Optional<User> findByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT u FROM User u WHERE u.accountName = :accountName AND u.deletedAt IS NULL")
  Optional<User> findByAccountName(@Param("accountName") String accountName);

  @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.deletedAt IS NULL")
  List<User> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.accountName = :accountName AND u.deletedAt IS NULL")
  boolean existsByAccountName(@Param("accountName") String accountName);
}
```

- [ ] **Step 2: Update `RoleQueryRepository.java`**

Add explicit `@Query` annotations for standard methods and maintenance methods:
```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleQueryRepository extends BaseQueryRepository<Role, UUID> {

  @Query("SELECT r FROM Role r WHERE r.id = :id AND r.deletedAt IS NULL")
  Optional<Role> findById(@Param("id") UUID id);

  @Query("SELECT r FROM Role r WHERE r.id = :id")
  Optional<Role> findByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.id = :id")
  boolean existsByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT r FROM Role r WHERE r.id = :id AND r.active = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveById(@Param("id") UUID id);

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
  Optional<Role> findByCode(@Param("code") String code);

  @Query("SELECT r FROM Role r WHERE r.code IN :codes AND r.deletedAt IS NULL")
  List<Role> findAllByCodeIn(@Param("codes") List<String> codes);

  @Query("SELECT COUNT(r) > 0 FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
  boolean existsByCode(@Param("code") String code);

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.active = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveByCode(@Param("code") String code);

  @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.deletedAt IS NULL")
  List<Role> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.active = true AND r.deletedAt IS NULL")
  List<Role> findAllActiveByIdIn(@Param("ids") List<UUID> ids);

  @Query("SELECT DISTINCT r FROM UserRole ur JOIN Role r ON ur.id.roleId = r.id WHERE ur.id.userId = :userId AND r.active = true AND r.deletedAt IS NULL")
  List<Role> findAllActiveByUserId(@Param("userId") UUID userId);

  @Query("SELECT DISTINCT r.id FROM UserRole ur JOIN Role r ON ur.id.roleId = r.id WHERE ur.id.userId = :userId AND r.active = true AND r.deletedAt IS NULL")
  List<UUID> findAllActiveRoleIdsByUserId(@Param("userId") UUID userId);
}
```

- [ ] **Step 3: Update `PermissionQueryRepository.java`**

Add explicit `@Query` annotations for standard methods and maintenance methods:
```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionQueryRepository extends BaseQueryRepository<Permission, UUID> {

  @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<Permission> findById(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.id = :id")
  Optional<Permission> findByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.id = :id")
  boolean existsByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
  Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  @Query("SELECT COUNT(p) > 0 FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
  boolean existsByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.deletedAt IS NULL")
  List<Permission> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.active = true AND p.deletedAt IS NULL")
  List<Permission> findAllActiveByIdIn(@Param("ids") List<UUID> ids);
}
```

- [ ] **Step 4: Commit Task 3 changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/repository/UserQueryRepository.java backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/RoleQueryRepository.java backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java
git commit -m "feat(repository): add explicit JPQL queries with soft delete filter and Tier 3 WithDeleted queries"
```

---

### Task 4: Update Services to Use Tier 3 Queries for Hard Delete & Tier 2 for Assigning

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/RoleService.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/PermissionService.java`

**Interfaces:**
- Consumes: `RoleQueryRepository.findByIdWithDeleted`, `PermissionQueryRepository.findByIdWithDeleted`, `PermissionQueryRepository.findAllActiveByIdIn`
- Produces: Updated service implementation for `hardDeleteRole`, `hardDeletePermission`, `assignPermissionsToRole`, `unassignPermissionsFromRole`.

- [ ] **Step 1: Update `RoleService.java`**

In `hardDeleteRole`: Change `roleQueryRepository.findById(id)` to `roleQueryRepository.findByIdWithDeleted(id)`.
In `assignPermissionsToRole` and `unassignPermissionsFromRole`: Change `permissionQueryRepository.findAllByIdIn(permissionIds)` to `permissionQueryRepository.findAllActiveByIdIn(permissionIds)`.

- [ ] **Step 2: Update `PermissionService.java`**

In `hardDeletePermission`: Change `permissionQueryRepository.findById(id)` to `permissionQueryRepository.findByIdWithDeleted(id)`.

- [ ] **Step 3: Commit Task 4 changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/RoleService.java backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/PermissionService.java
git commit -m "feat(service): update hard-delete procedures to use WithDeleted queries and assign permissions using active filter"
```

---

### Task 5: Verification & Unit Test Suite Updates

**Files:**
- Modify: `backend/src/test/java/c4f/vannang/vaops/modules/identity/internal/domain/UserTest.java` (if softDelete assertions check active)
- Modify: Use-case tests as needed

**Interfaces:**
- Consumes: Full test suite
- Produces: Clean passing build and tests verifying soft delete and activatable behavior.

- [ ] **Step 1: Run full test suite**

Run command: `./mvnw test` or `mvn test` in `backend` directory.
Expected: PASS for unit and integration tests.

- [ ] **Step 2: Verify test assertions and fix if needed**

If tests assert that `softDelete()` sets `active = false`, update assertion to expect `active = true` (or check `deletedAt IS NOT NULL`), since `softDelete()` no longer modifies `active`.

- [ ] **Step 3: Final Commit**

```bash
git add .
git commit -m "test: update unit and use-case tests for soft delete standardization"
```

---
