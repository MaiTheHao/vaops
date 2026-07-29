# Technical Design: Shared Pagination Infrastructure & Specification Pattern

**Author:** Antigravity AI  
**Date:** 2026-07-29  
**Status:** Approved  
**Scope:** Authorization Module & Identity Module  

---

## 1. Overview & Goals

Currently, query methods in the `authorization` and `identity` modules suffer from scattered get-list methods with zero pagination or dynamic search capabilities. 

This design establishes a unified **Shared Pagination Infrastructure** and **JPA Specification Pattern** across the backend:
1. Shared `PageResponse<T>` wrapper and `BaseSearchCriteria` DTOs.
2. Read-only `BaseQueryRepository<T, ID>` extending Spring Data's `Repository<T, ID>` and `JpaSpecificationExecutor<T>`.
3. Dedicated Specification class per domain (`PermissionSpecification`, `RoleSpecification`, `UserSpecification`). Each class contains modular static specification methods and a master `search(Criteria criteria)` composite method.
4. Updated Service methods for searching and listing entities.

---

## 2. Shared Infrastructure

Location: `backend/src/main/java/c4f/vannang/vaops/shared/`

### 2.1. `PageResponse<T>`
Package: `c4f.vannang.vaops.shared.pagination`

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

### 2.2. `BaseSearchCriteria`
Package: `c4f.vannang.vaops.shared.pagination`

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

### 2.3. `BaseQueryRepository<T, ID>`
Package: `c4f.vannang.vaops.shared.repository`

```java
package c4f.vannang.vaops.shared.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface BaseQueryRepository<T, ID> extends Repository<T, ID>, JpaSpecificationExecutor<T> {
}
```

---

## 3. Module Specifications & Search Criteria

### 3.1. Authorization Module (`Permission` & `Role`)

#### 3.1.1. `PermissionSearchCriteria` & `PermissionSpecification`
Package: `c4f.vannang.vaops.modules.authorization.internal.dto.criteria` & `specification`

- **Criteria DTO:**
```java
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

- **Specification Class:**
```java
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

#### 3.1.2. `RoleSearchCriteria` & `RoleSpecification`

- **Specification Class (`RoleSpecification`):**
  - `isNotDeleted()`
  - `isActive(Boolean active)`
  - `hasCode(String code)`
  - `hasKeyword(String keyword)` (matches `code`, `name`, `description`)
  - `search(RoleSearchCriteria criteria)` — master composite specification.

---

### 3.2. Identity Module (`User`)

#### 3.2.1. `UserSearchCriteria` & `UserSpecification`
Package: `c4f.vannang.vaops.modules.identity.internal.dto.criteria` & `specification`

- **Specification Class (`UserSpecification`):**
  - `isNotDeleted()`
  - `isActive(Boolean active)`
  - `hasAccountName(String accountName)`
  - `hasKeyword(String keyword)` (matches `accountName.value`, `displayName`)
  - `search(UserSearchCriteria criteria)` — master composite specification.

---

## 4. Repositories & Services Integration

### 4.1. Repository Updates
Repositories in `authorization` and `identity` modules will extend `BaseQueryRepository`:
- `PermissionQueryRepository extends BaseQueryRepository<Permission, UUID>`
- `RoleQueryRepository extends BaseQueryRepository<Role, UUID>`
- `UserQueryRepository extends BaseQueryRepository<User, UUID>`

### 4.2. Service Methods
- `PermissionService`: Add `PageResponse<PermissionResponse> searchPermissions(PermissionSearchCriteria criteria)`
- `RoleService`: Add `PageResponse<RoleResponse> searchRoles(RoleSearchCriteria criteria)`
- `UserService`: Add `PageResponse<UserResponse> searchUsers(UserSearchCriteria criteria)`

---

## 5. Verification Plan

1. Compile the project with `./mvnw clean compile` to ensure zero syntax or generics errors.
2. Execute existing test suite `./mvnw test` to ensure no regressions.
3. Validate that `PageResponse` JSON mapping contains `hasNext` and `hasPrevious` without redundant `first`/`last` fields.
