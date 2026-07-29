# Apply Specification & Remove Legacy READ-Many Methods Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove legacy scattered READ-many query methods across repository and service layers, replacing them with dynamic Specification search via `PermissionSearchCriteria`, `RoleSearchCriteria`, and `UserRoleSearchCriteria`.

**Architecture:** Extend existing Specification classes with `hasUserId` and `hasRoleId` filters, add `UserRoleSearchCriteria` & `UserRoleSpecification`, extend `BaseQueryRepository` for `UserRoleQueryRepository`, delete legacy read-many methods, and update service classes to strictly use `search*()` methods.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA (JpaSpecificationExecutor, CriteriaBuilder), Lombok.

## Global Constraints

- **No tests update:** Per user directive, skip creating or updating unit/integration tests for now.
- **No extra Base abstractions:** Hard-code specifications directly in module classes without creating new shared BaseRepository/BaseSpecification hierarchies in `shared/`.

---

### Task 1: UserRole Specification & Repository Refactoring

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/UserRoleSearchCriteria.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/UserRoleSpecification.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/UserRoleQueryRepository.java`

**Interfaces:**
- Produces: `UserRoleSearchCriteria`, `UserRoleSpecification`, and updated `UserRoleQueryRepository` extending `BaseQueryRepository<UserRole, UserRoleId>`.

- [ ] **Step 1: Create `UserRoleSearchCriteria.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record UserRoleSearchCriteria(
    UUID userId,
    UUID roleId,
    List<UUID> roleIds,
    Boolean isRevoked,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
  public Pageable toPageable() {
    int validPage = Math.max(0, page);
    int validSize = size <= 0 ? 20 : size;
    String validSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
    Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
    return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
  }
}
```

- [ ] **Step 2: Create `UserRoleSpecification.java`**

```java
package c4f.vannang.vaops.modules.authorization.internal.dto; // or internal.repository

package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.UserRoleSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class UserRoleSpecification {

  public static Specification<UserRole> hasUserId(UUID userId) {
    return (root, query, cb) -> userId == null ? null : cb.equal(root.get("id").get("userId"), userId);
  }

  public static Specification<UserRole> hasRoleId(UUID roleId) {
    return (root, query, cb) -> roleId == null ? null : cb.equal(root.get("id").get("roleId"), roleId);
  }

  public static Specification<UserRole> hasRoleIdsIn(List<UUID> roleIds) {
    return (root, query, cb) -> (roleIds == null || roleIds.isEmpty()) ? null : root.get("id").get("roleId").in(roleIds);
  }

  public static Specification<UserRole> isNotRevoked() {
    return (root, query, cb) -> cb.isNull(root.get("revokedAt"));
  }

  public static Specification<UserRole> isRevoked(Boolean revoked) {
    if (revoked == null) return null;
    return revoked ? (root, query, cb) -> cb.isNotNull(root.get("revokedAt")) : isNotRevoked();
  }

  public static Specification<UserRole> search(UserRoleSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.where(isNotRevoked());
    }
    return Specification.where(hasUserId(criteria.userId()))
        .and(hasRoleId(criteria.roleId()))
        .and(hasRoleIdsIn(criteria.roleIds()))
        .and(isRevoked(criteria.isRevoked()));
  }
}
```

- [ ] **Step 3: Update `UserRoleQueryRepository.java`**

Remove `findAllByUserId`, `findAllActiveByUserId`, `findAllActiveByRoleId`, `findAllByUserIdAndRoleIdIn`. Extend `BaseQueryRepository<UserRole, UserRoleId>`.

```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface UserRoleQueryRepository extends BaseQueryRepository<UserRole, UserRoleId> {

  Optional<UserRole> findById(UserRoleId id);

  @Query("SELECT CASE WHEN EXISTS (" +
         "SELECT 1 FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId = :roleId AND ur.revokedAt IS NULL) THEN true ELSE false END")
  boolean existsActiveByUserIdAndRoleId(@Param("userId") UUID userId, @Param("roleId") UUID roleId);
}
```

---

### Task 2: Permission Criteria, Specification & Repository Refactoring

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/PermissionSearchCriteria.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionSpecification.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java`

- [ ] **Step 1: Update `PermissionSearchCriteria.java`**

Add `UUID userId`, `UUID roleId` fields:

```java
package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PermissionSearchCriteria(
    String keyword,
    String resource,
    String action,
    Boolean isActive,
    UUID userId,
    UUID roleId,
    Instant createdFrom,
    Instant createdTo,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
  public Pageable toPageable() {
    int validPage = Math.max(0, page);
    int validSize = size <= 0 ? 20 : size;
    String validSortBy = (sortBy == null || sortBy.isBlank()) ? "resource" : sortBy;
    Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
    return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
  }
}
```

- [ ] **Step 2: Update `PermissionSpecification.java`**

Add `hasRoleId(UUID roleId)` and `hasUserId(UUID userId)` and update `search`:

```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import jakarta.persistence.criteria.Join;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public class PermissionSpecification {

  public static Specification<Permission> hasKeyword(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) return null;
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("resource")), pattern),
          cb.like(cb.lower(root.get("action")), pattern),
          cb.like(cb.lower(root.get("description")), pattern)
      );
    };
  }

  public static Specification<Permission> hasResource(String resource) {
    return (root, query, cb) -> (resource == null || resource.isBlank()) ? null : cb.equal(root.get("resource"), resource);
  }

  public static Specification<Permission> hasAction(String action) {
    return (root, query, cb) -> (action == null || action.isBlank()) ? null : cb.equal(root.get("action"), action);
  }

  public static Specification<Permission> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<Permission> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }

  public static Specification<Permission> createdAfter(Instant from) {
    return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<Permission> createdBefore(Instant to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<Permission> hasRoleId(UUID roleId) {
    return (root, query, cb) -> {
      if (roleId == null) return null;
      query.distinct(true);
      Join<Permission, Role> roles = root.join("roles");
      return cb.and(
          cb.equal(roles.get("id"), roleId),
          cb.equal(roles.get("isActive"), true),
          cb.isNull(roles.get("deletedAt"))
      );
    };
  }

  public static Specification<Permission> hasUserId(UUID userId) {
    return (root, query, cb) -> {
      if (userId == null) return null;
      query.distinct(true);
      Join<Permission, Role> roles = root.join("roles");
      Join<Role, UserRole> userRoles = roles.join("userRoles");
      return cb.and(
          cb.equal(userRoles.get("id").get("userId"), userId),
          cb.isNull(userRoles.get("revokedAt")),
          cb.equal(roles.get("isActive"), true),
          cb.isNull(roles.get("deletedAt"))
      );
    };
  }

  public static Specification<Permission> search(PermissionSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.where(isNotDeleted());
    }
    return Specification.where(isNotDeleted())
        .and(hasKeyword(criteria.keyword()))
        .and(hasResource(criteria.resource()))
        .and(hasAction(criteria.action()))
        .and(isActive(criteria.isActive()))
        .and(hasRoleId(criteria.roleId()))
        .and(hasUserId(criteria.userId()))
        .and(createdAfter(criteria.createdFrom()))
        .and(createdBefore(criteria.createdTo()));
  }
}
```

- [ ] **Step 3: Update `PermissionQueryRepository.java`**

Remove `findAllActive()`, `findActivePermissionsByRoleId()`, `findActivePermissionsByUserId()`:

```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface PermissionQueryRepository extends BaseQueryRepository<Permission, UUID> {

  Optional<Permission> findById(UUID id);

  @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.isActive = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveById(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
  Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  boolean existsByResourceAndAction(String resource, String action);

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.isActive = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  List<Permission> findAllByIdIn(List<UUID> ids);

  @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.isActive = true AND p.deletedAt IS NULL")
  List<Permission> findAllActiveByIds(@Param("ids") List<UUID> ids);

  @Query("SELECT CASE WHEN EXISTS (" +
         "SELECT 1 FROM Permission p JOIN p.roles r JOIN UserRole ur ON ur.id.roleId = r.id " +
         "WHERE ur.id.userId = :userId AND p.resource = :resource AND p.action = :action " +
         "AND ur.revokedAt IS NULL AND r.isActive = true AND r.deletedAt IS NULL " +
         "AND p.isActive = true AND p.deletedAt IS NULL) THEN true ELSE false END")
  boolean hasPermission(@Param("userId") UUID userId, @Param("resource") String resource, @Param("action") String action);
}
```

---

### Task 3: Role Criteria, Specification & Repository Refactoring

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/RoleSearchCriteria.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/RoleSpecification.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/RoleQueryRepository.java`

- [ ] **Step 1: Update `RoleSearchCriteria.java`**

Add `UUID userId` field:

```java
package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record RoleSearchCriteria(
    String keyword,
    String code,
    Boolean isActive,
    UUID userId,
    Instant createdFrom,
    Instant createdTo,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {
  public Pageable toPageable() {
    int validPage = Math.max(0, page);
    int validSize = size <= 0 ? 20 : size;
    String validSortBy = (sortBy == null || sortBy.isBlank()) ? "code" : sortBy;
    Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
    return PageRequest.of(validPage, validSize, Sort.by(direction, validSortBy));
  }
}
```

- [ ] **Step 2: Update `RoleSpecification.java`**

Add `hasUserId(UUID userId)` and update `search`:

```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleSearchCriteria;
import jakarta.persistence.criteria.Join;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification {

  public static Specification<Role> hasKeyword(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) return null;
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("code")), pattern),
          cb.like(cb.lower(root.get("description")), pattern)
      );
    };
  }

  public static Specification<Role> hasCode(String code) {
    return (root, query, cb) -> (code == null || code.isBlank()) ? null : cb.equal(root.get("code"), code);
  }

  public static Specification<Role> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<Role> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }

  public static Specification<Role> createdAfter(Instant from) {
    return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<Role> createdBefore(Instant to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<Role> hasUserId(UUID userId) {
    return (root, query, cb) -> {
      if (userId == null) return null;
      query.distinct(true);
      Join<Role, UserRole> userRoles = root.join("userRoles");
      return cb.and(
          cb.equal(userRoles.get("id").get("userId"), userId),
          cb.isNull(userRoles.get("revokedAt"))
      );
    };
  }

  public static Specification<Role> search(RoleSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.where(isNotDeleted());
    }
    return Specification.where(isNotDeleted())
        .and(hasKeyword(criteria.keyword()))
        .and(hasCode(criteria.code()))
        .and(isActive(criteria.isActive()))
        .and(hasUserId(criteria.userId()))
        .and(createdAfter(criteria.createdFrom()))
        .and(createdBefore(criteria.createdTo()));
  }
}
```

- [ ] **Step 3: Update `RoleQueryRepository.java`**

Remove `findAllActive()`, `findActiveRolesByUserId()`:

```java
package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface RoleQueryRepository extends BaseQueryRepository<Role, UUID> {

  Optional<Role> findById(UUID id);

  @Query("SELECT r FROM Role r WHERE r.id = :id AND r.isActive = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveById(@Param("id") UUID id);

  Optional<Role> findByCode(String code);

  boolean existsByCode(String code);

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.isActive = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveByCode(@Param("code") String code);

  List<Role> findAllByIdIn(List<UUID> ids);

  @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.isActive = true AND r.deletedAt IS NULL")
  List<Role> findAllActiveByIds(@Param("ids") List<UUID> ids);
}
```

---

### Task 4: Service Layer Refactoring & Compilation Verification

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/PermissionService.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/RoleService.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/UserRoleService.java` (if necessary)

- [ ] **Step 1: Remove `listPermissions()` and `getUserPermissions()` from `PermissionService.java`**

Remove lines 91 to 105 in `PermissionService.java`.

- [ ] **Step 2: Remove `listRoles()` and `getUserRoles()` from `RoleService.java`**

Remove lines 84 to 96 in `RoleService.java`.

- [ ] **Step 3: Compile and Verify Project Build**

Run command:
`./mvnw compile`
Expected output: BUILD SUCCESS with 0 compilation errors.
