# Shared Pagination Infrastructure & Specification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a shared pagination & JPA specification infrastructure and integrate dynamic search/filter + pagination across the `authorization` and `identity` modules using a 1-class-per-domain specification pattern with static filter methods and a master `search(criteria)` composite method.

**Architecture:** Create `PageResponse<T>` and `BaseSearchCriteria` in `c4f.vannang.vaops.shared.pagination`, and `BaseQueryRepository<T, ID>` in `c4f.vannang.vaops.shared.repository` extending `Repository<T, ID>` and `JpaSpecificationExecutor<T>`. For each domain (`Permission`, `Role`, `User`), build a Criteria DTO and a Specification class containing modular filter methods plus a composite `search(criteria)` method. Update Query Repositories to extend `BaseQueryRepository` and add `search...` methods in Service layers returning `PageResponse<...Response>`.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Data JPA, Hibernate, JUnit 5.

## Global Constraints
- `PageResponse` contains: `content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`. Redundant `first` and `last` booleans are omitted.
- Default `page` indexing is 0-indexed (Spring Data default).
- Master composite method in Specification classes MUST be named `search(criteria)`.
- Query repositories remain read-only by extending `BaseQueryRepository<T, ID>` (no `save`/`delete` methods exposed).

---

### Task 1: Create Shared Infrastructure Components (`PageResponse`, `BaseSearchCriteria`, `BaseQueryRepository`)

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/pagination/PageResponse.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/pagination/BaseSearchCriteria.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/repository/BaseQueryRepository.java`

**Interfaces:**
- Consumes: `org.springframework.data.domain.Page`, `Pageable`, `Sort`, `JpaSpecificationExecutor`, `Repository`
- Produces: `PageResponse<T>`, `BaseSearchCriteria`, `BaseQueryRepository<T, ID>`

- [ ] **Step 1: Write code for `PageResponse`**

```java
package c4f.vannang.vaops.shared.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static <T> PageResponse<T> from(Page<T> springPage) {
        return new PageResponse<>(
            springPage.getContent(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.hasNext(),
            springPage.hasPrevious()
        );
    }

    public static <T, R> PageResponse<R> from(Page<T> springPage, java.util.function.Function<T, R> mapper) {
        List<R> mappedContent = springPage.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
            mappedContent,
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.hasNext(),
            springPage.hasPrevious()
        );
    }
}
```

- [ ] **Step 2: Write code for `BaseSearchCriteria`**

```java
package c4f.vannang.vaops.shared.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record BaseSearchCriteria(
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {
    public BaseSearchCriteria {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "ASC";
    }

    public Pageable toPageable() {
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
```

- [ ] **Step 3: Write code for `BaseQueryRepository`**

```java
package c4f.vannang.vaops.shared.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface BaseQueryRepository<T, ID> extends Repository<T, ID>, JpaSpecificationExecutor<T> {
}
```

- [ ] **Step 4: Verify Compilation**

Run: `./mvnw clean compile`  
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/shared/pagination/ backend/src/main/java/c4f/vannang/vaops/shared/repository/
git commit -m "feat(shared): add PageResponse, BaseSearchCriteria and BaseQueryRepository infrastructure"
```

---

### Task 2: Implement Authorization Module Specifications, Repositories, & Services (`Permission` & `Role`)

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/criteria/PermissionSearchCriteria.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/specification/PermissionSpecification.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/criteria/RoleSearchCriteria.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/specification/RoleSpecification.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/RoleQueryRepository.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/PermissionService.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/RoleService.java`

**Interfaces:**
- Consumes: `BaseQueryRepository`, `BaseSearchCriteria`, `PageResponse`
- Produces: `PermissionSearchCriteria`, `PermissionSpecification.search()`, `RoleSearchCriteria`, `RoleSpecification.search()`, `PermissionService.searchPermissions()`, `RoleService.searchRoles()`

- [ ] **Step 1: Create `PermissionSearchCriteria` & `PermissionSpecification`**

`PermissionSearchCriteria.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.dto.criteria;

import c4f.vannang.vaops.shared.pagination.BaseSearchCriteria;
import org.springframework.data.domain.Pageable;

public record PermissionSearchCriteria(
    String keyword,
    String resource,
    String action,
    Boolean isActive,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {
    public Pageable toPageable() {
        return new BaseSearchCriteria(page, size, sortBy, sortDirection).toPageable();
    }
}
```

`PermissionSpecification.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.specification;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.criteria.PermissionSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public final class PermissionSpecification {

    private PermissionSpecification() {}

    public static Specification<Permission> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Permission> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Permission> hasResource(String resource) {
        return (root, query, cb) -> (resource == null || resource.isBlank()) ? null 
            : cb.equal(root.get("resource"), resource);
    }

    public static Specification<Permission> hasAction(String action) {
        return (root, query, cb) -> (action == null || action.isBlank()) ? null 
            : cb.equal(root.get("action"), action);
    }

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

    public static Specification<Permission> search(PermissionSearchCriteria criteria) {
        if (criteria == null) {
            return isNotDeleted();
        }
        return Specification
            .where(isNotDeleted())
            .and(isActive(criteria.isActive()))
            .and(hasResource(criteria.resource()))
            .and(hasAction(criteria.action()))
            .and(hasKeyword(criteria.keyword()));
    }
}
```

- [ ] **Step 2: Create `RoleSearchCriteria` & `RoleSpecification`**

`RoleSearchCriteria.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.dto.criteria;

import c4f.vannang.vaops.shared.pagination.BaseSearchCriteria;
import org.springframework.data.domain.Pageable;

public record RoleSearchCriteria(
    String keyword,
    String code,
    Boolean isActive,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {
    public Pageable toPageable() {
        return new BaseSearchCriteria(page, size, sortBy, sortDirection).toPageable();
    }
}
```

`RoleSpecification.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.specification;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.criteria.RoleSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public final class RoleSpecification {

    private RoleSpecification() {}

    public static Specification<Role> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<Role> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Role> hasCode(String code) {
        return (root, query, cb) -> (code == null || code.isBlank()) ? null 
            : cb.equal(root.get("code"), code);
    }

    public static Specification<Role> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Role> search(RoleSearchCriteria criteria) {
        if (criteria == null) {
            return isNotDeleted();
        }
        return Specification
            .where(isNotDeleted())
            .and(isActive(criteria.isActive()))
            .and(hasCode(criteria.code()))
            .and(hasKeyword(criteria.keyword()));
    }
}
```

- [ ] **Step 3: Update `PermissionQueryRepository` & `RoleQueryRepository` to extend `BaseQueryRepository`**

Update `PermissionQueryRepository.java`:
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

  @Query("SELECT p FROM Permission p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.resource ASC, p.action ASC")
  List<Permission> findAllActive();

  @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId AND r.isActive = true AND r.deletedAt IS NULL AND p.isActive = true AND p.deletedAt IS NULL")
  List<Permission> findActivePermissionsByRoleId(@Param("roleId") UUID roleId);

  @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN UserRole ur ON ur.id.roleId = r.id WHERE ur.id.userId = :userId AND ur.revokedAt IS NULL AND r.isActive = true AND r.deletedAt IS NULL AND p.isActive = true AND p.deletedAt IS NULL")
  List<Permission> findActivePermissionsByUserId(@Param("userId") UUID userId);

  @Query("SELECT CASE WHEN EXISTS (" +
         "SELECT 1 FROM Permission p JOIN p.roles r JOIN UserRole ur ON ur.id.roleId = r.id " +
         "WHERE ur.id.userId = :userId AND p.resource = :resource AND p.action = :action " +
         "AND ur.revokedAt IS NULL AND r.isActive = true AND r.deletedAt IS NULL " +
         "AND p.isActive = true AND p.deletedAt IS NULL) THEN true ELSE false END")
  boolean hasPermission(@Param("userId") UUID userId, @Param("resource") String resource, @Param("action") String action);
}
```

Update `RoleQueryRepository.java`:
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

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.isActive = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveByCode(@Param("code") String code);

  boolean existsByCode(String code);

  List<Role> findAllByIdIn(List<UUID> ids);

  @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.isActive = true AND r.deletedAt IS NULL")
  List<Role> findAllActiveByIds(@Param("ids") List<UUID> ids);

  @Query("SELECT r FROM Role r WHERE r.isActive = true AND r.deletedAt IS NULL ORDER BY r.code ASC")
  List<Role> findAllActive();

  @Query("SELECT DISTINCT r FROM Role r JOIN UserRole ur ON ur.id.roleId = r.id WHERE ur.id.userId = :userId AND ur.revokedAt IS NULL AND r.isActive = true AND r.deletedAt IS NULL")
  List<Role> findActiveRolesByUserId(@Param("userId") UUID userId);
}
```

- [ ] **Step 4: Add `searchPermissions` in `PermissionService` & `searchRoles` in `RoleService`**

Add method to `PermissionService.java`:
```java
  @Transactional(readOnly = true)
  public PageResponse<PermissionResponse> searchPermissions(PermissionSearchCriteria criteria) {
    Specification<Permission> spec = PermissionSpecification.search(criteria);
    Pageable pageable = criteria.toPageable();
    Page<Permission> pageResult = permissionQueryRepository.findAll(spec, pageable);
    return PageResponse.from(pageResult, permissionResponseMapper::toResponse);
  }
```

Add method to `RoleService.java`:
```java
  @Transactional(readOnly = true)
  public PageResponse<RoleResponse> searchRoles(RoleSearchCriteria criteria) {
    Specification<Role> spec = RoleSpecification.search(criteria);
    Pageable pageable = criteria.toPageable();
    Page<Role> pageResult = roleQueryRepository.findAll(spec, pageable);
    return PageResponse.from(pageResult, roleResponseMapper::toResponse);
  }
```

- [ ] **Step 5: Verify Compilation**

Run: `./mvnw clean compile`  
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/
git commit -m "feat(authorization): add Specification search and pagination for Permission and Role"
```

---

### Task 3: Implement Identity Module Specification, Repository, & Service (`User`)

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/dto/criteria/UserSearchCriteria.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/specification/UserSpecification.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/repository/UserQueryRepository.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/service/UserService.java` (or equivalent query service)

**Interfaces:**
- Consumes: `BaseQueryRepository`, `BaseSearchCriteria`, `PageResponse`
- Produces: `UserSearchCriteria`, `UserSpecification.search()`, `UserService.searchUsers()`

- [ ] **Step 1: Create `UserSearchCriteria` & `UserSpecification`**

`UserSearchCriteria.java`:
```java
package c4f.vannang.vaops.modules.identity.internal.dto.criteria;

import c4f.vannang.vaops.shared.pagination.BaseSearchCriteria;
import org.springframework.data.domain.Pageable;

public record UserSearchCriteria(
    String keyword,
    String accountName,
    Boolean isActive,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {
    public Pageable toPageable() {
        return new BaseSearchCriteria(page, size, sortBy, sortDirection).toPageable();
    }
}
```

`UserSpecification.java`:
```java
package c4f.vannang.vaops.modules.identity.internal.specification;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.criteria.UserSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<User> hasAccountName(String accountName) {
        return (root, query, cb) -> (accountName == null || accountName.isBlank()) ? null 
            : cb.equal(root.get("accountName").get("value"), accountName);
    }

    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("accountName").get("value")), pattern),
                cb.like(cb.lower(root.get("displayName")), pattern)
            );
        };
    }

    public static Specification<User> search(UserSearchCriteria criteria) {
        if (criteria == null) {
            return isNotDeleted();
        }
        return Specification
            .where(isNotDeleted())
            .and(isActive(criteria.isActive()))
            .and(hasAccountName(criteria.accountName()))
            .and(hasKeyword(criteria.keyword()));
    }
}
```

- [ ] **Step 2: Update `UserQueryRepository` to extend `BaseQueryRepository`**

Update `UserQueryRepository.java`:
```java
package c4f.vannang.vaops.modules.identity.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.vo.AccountName;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface UserQueryRepository extends BaseQueryRepository<User, UUID> {

  Optional<User> findById(UUID id);

  @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true AND u.deletedAt IS NULL")
  Optional<User> findActiveById(@Param("id") UUID id);

  List<User> findAllByIdIn(List<UUID> ids);

  @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.isActive = true AND u.deletedAt IS NULL")
  List<User> findAllActiveByIds(@Param("ids") List<UUID> ids);

  Optional<User> findByAccountName(AccountName accountName);

  @Query("SELECT u FROM User u WHERE u.accountName = :accountName AND u.isActive = true AND u.deletedAt IS NULL")
  Optional<User> findActiveByAccountName(@Param("accountName") AccountName accountName);

  boolean existsByAccountName(AccountName accountName);

  @Query("SELECT CASE WHEN EXISTS (SELECT 1 FROM User u WHERE u.accountName = :accountName AND u.isActive = true AND u.deletedAt IS NULL) THEN true ELSE false END")
  boolean existsActiveByAccountName(@Param("accountName") AccountName accountName);
}
```

- [ ] **Step 3: Update `UserService` (or add search method to user service)**

Add `searchUsers` method in `UserService.java`:
```java
  @Transactional(readOnly = true)
  public PageResponse<UserResponse> searchUsers(UserSearchCriteria criteria) {
    Specification<User> spec = UserSpecification.search(criteria);
    Pageable pageable = criteria.toPageable();
    Page<User> pageResult = userQueryRepository.findAll(spec, pageable);
    return PageResponse.from(pageResult, userResponseMapper::toResponse);
  }
```

- [ ] **Step 4: Verify Compilation & Test Suite**

Run: `./mvnw clean test`  
Expected: BUILD SUCCESS with 100% passing tests.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/identity/
git commit -m "feat(identity): add Specification search and pagination for User"
```
