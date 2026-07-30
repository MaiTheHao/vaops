# Design Specification: Refactoring RolePermission & UserRole Domain Entities & Specification Performance Optimization

**Date**: 2026-07-30  
**Author**: Antigravity & User Pair Programming  
**Status**: Draft / Spec Review  

---

## 1. Overview & Objectives

In the `authorization` module of VAOPS, queries in `PermissionSpecification.java` previously suffered from JPA join performance overhead due to implicit `@ManyToMany` mappings between `Role` and `Permission`.

This design refactors the domain model to:
1. Treat JPA Entities directly as Domain Entities (Rich Domain Model using JPA).
2. Explicitly model intermediate join tables (`role_permissions` and `user_roles`) using `@EmbeddedId` composite keys (`RolePermissionId`, `UserRoleId`).
3. Optimize `PermissionSpecification` subqueries to query intermediate entity composite keys directly, avoiding redundant SQL `JOIN` statements.

---

## 2. Domain Model Refactoring (`modules/authorization/internal/domain`)

### 2.1. `RolePermissionId.java` (`[NEW]` in `domain/id/`)
- Class annotated with `@Embeddable` implementing `Serializable`.
- Properties:
  - `UUID roleId`
  - `UUID permissionId`
- Equals, hashCode, and standard constructors.

### 2.2. `RolePermission.java` (`[NEW]` in `domain/`)
- Class annotated with `@Entity` and `@Table(name = "role_permissions")`.
- Primary Key: `@EmbeddedId private RolePermissionId id;`
- Attributes:
  - `Instant assignedAt` (`@Column(name = "assigned_at", nullable = false)`)
  - `UUID assignedBy` (`@Column(name = "assigned_by")`)
- Factory Method: `RolePermission.assign(UUID roleId, UUID permissionId, UUID assignedBy)`.

### 2.3. `UserRole.java` & `UserRoleId.java` Alignment
- `UserRole` and `UserRoleId` are verified and updated to adhere to the exact same design conventions:
  - `@EmbeddedId UserRoleId id`
  - Fields: `assignedAt`, `assignedBy`.

### 2.4. `Role.java` & `Permission.java` Clean Up
- Remove implicit `@ManyToMany` fields (`private Set<Permission> permissions`) from `Role.java`.
- Relationships are managed explicitly via `RolePermission` repository/domain operations.

---

## 3. Specification Query Optimization (`repository/spec/PermissionSpecification.java`)

### 3.1. `hasRoleId(UUID roleId)`
```java
public static Specification<Permission> hasRoleId(UUID roleId) {
  return (root, query, cb) -> {
    if (roleId == null) return cb.conjunction();
    Subquery<UUID> subquery = query.subquery(UUID.class);
    Root<RolePermission> rpRoot = subquery.from(RolePermission.class);
    Root<Role> roleRoot = subquery.from(Role.class);
    subquery.select(rpRoot.get("id").get("permissionId"))
            .where(
                cb.equal(rpRoot.get("id").get("roleId"), roleId),
                cb.equal(roleRoot.get("id"), roleId),
                cb.isTrue(roleRoot.get("isActive")),
                cb.isNull(roleRoot.get("deletedAt"))
            );
    return root.get("id").in(subquery);
  };
}
```

### 3.2. `hasUserId(UUID userId)`
```java
public static Specification<Permission> hasUserId(UUID userId) {
  return (root, query, cb) -> {
    if (userId == null) return cb.conjunction();
    Subquery<UUID> subquery = query.subquery(UUID.class);
    Root<RolePermission> rpRoot = subquery.from(RolePermission.class);
    Root<UserRole> urRoot = subquery.from(UserRole.class);
    Root<Role> roleRoot = subquery.from(Role.class);
    subquery.select(rpRoot.get("id").get("permissionId"))
            .where(
                cb.equal(urRoot.get("id").get("userId"), userId),
                cb.equal(urRoot.get("id").get("roleId"), rpRoot.get("id").get("roleId")),
                cb.equal(roleRoot.get("id"), rpRoot.get("id").get("roleId")),
                cb.isTrue(roleRoot.get("isActive")),
                cb.isNull(roleRoot.get("deletedAt"))
            );
    return root.get("id").in(subquery);
  };
}
```

---

## 4. Verification & Testing Strategy
- Compile the backend code using Maven/Gradle build.
- Validate unit/integration tests for authorization permissions resolution.
- Verify generated SQL logs to confirm zero unnecessary `JOIN` operations on implicit JPA tables.
