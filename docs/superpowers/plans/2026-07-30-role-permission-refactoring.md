# RolePermission Entity & Specification Optimization Implementation Plan (Manual Learning Guide)

> **For developer / self-study worker:** This plan is designed for step-by-step manual implementation to master Rich Domain Modeling using JPA entities with composite primary keys (`@EmbeddedId`) and writing optimized JPA Specification subqueries.

**Goal:** Refactor the authorization module by replacing implicit JPA `@ManyToMany` relations with explicit `RolePermission` domain entity and optimizing `PermissionSpecification` subquery performance.

**Architecture:** JPA entities serve directly as Domain entities. Intermediate join tables are explicitly modeled with `@EmbeddedId` composite keys (`RolePermissionId`, `UserRoleId`). JPA Specifications query intermediate entity composite keys directly in subqueries to avoid unnecessary SQL `JOIN` overhead.

**Tech Stack:** Java 21, Spring Data JPA, Jakarta Persistence Criteria API, Hibernate, JUnit 5.

## Global Constraints
- **Domain Layer Rule**: JPA Entities ARE Domain Entities. No redundant mapping DTOs or duplicate domain models.
- **Join Table Modeling**: All intermediate join tables MUST use `@EmbeddedId` composite keys located in `domain/id/`.
- **Criteria API Subqueries**: `PermissionSpecification` subqueries MUST query primary keys directly without `@ManyToMany` joins.

---

### Task 1: Create Composite Primary Key `RolePermissionId`

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/id/RolePermissionId.java`

**Interfaces:**
- Produces: `RolePermissionId` embeddable class with composite key `roleId` and `permissionId`.

- [ ] **Step 1: Create `RolePermissionId.java`**

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/id/RolePermissionId.java` with exact content:

```java
package c4f.vannang.vaops.modules.authorization.internal.domain.id;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermissionId implements Serializable {

  @Column(name = "role_id", nullable = false)
  private UUID roleId;

  @Column(name = "permission_id", nullable = false)
  private UUID permissionId;

  public RolePermissionId(UUID roleId, UUID permissionId) {
    this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
    this.permissionId = Objects.requireNonNull(permissionId, "permissionId must not be null");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RolePermissionId that = (RolePermissionId) o;
    return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, permissionId);
  }
}
```

- [ ] **Step 2: Verify Compilation**

Run build command in backend:
```bash
./mvnw clean compile -pl backend
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/id/RolePermissionId.java
git commit -m "feat(auth): add RolePermissionId composite key"
```

---

### Task 2: Create Intermediate Entity `RolePermission`

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/RolePermission.java`

**Interfaces:**
- Consumes: `RolePermissionId` from Task 1
- Produces: `RolePermission` entity with `@EmbeddedId` and assignment metadata.

- [ ] **Step 1: Create `RolePermission.java`**

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/RolePermission.java` with exact content:

```java
package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.RolePermissionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission {

  @EmbeddedId
  private RolePermissionId id;

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  @Column(name = "assigned_by")
  private UUID assignedBy;

  public static RolePermission assign(UUID roleId, UUID permissionId, UUID assignedBy) {
    RolePermission rp = new RolePermission();
    rp.id = new RolePermissionId(roleId, permissionId);
    rp.assignedAt = Instant.now();
    rp.assignedBy = assignedBy;
    return rp;
  }
}
```

- [ ] **Step 2: Verify Compilation**

Run:
```bash
./mvnw clean compile -pl backend
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/RolePermission.java
git commit -m "feat(auth): create explicit RolePermission domain entity"
```

---

### Task 3: Refactor `Role` Domain Entity to Remove Implicit `@ManyToMany`

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Role.java`

**Interfaces:**
- Removes: `@ManyToMany private Set<Permission> permissions` and implicit `@JoinTable`.

- [ ] **Step 1: Update `Role.java`**

Remove the `@ManyToMany` mapping and collection operations from `Role.java`:

Remove lines 46-52:
```java
-  @ManyToMany
-  @JoinTable(
-      name = "role_permissions",
-      joinColumns = @JoinColumn(name = "role_id"),
-      inverseJoinColumns = @JoinColumn(name = "permission_id")
-  )
-  private Set<Permission> permissions = new HashSet<>();
```

Remove methods that rely directly on the `permissions` collection (`assignPermission`, `assignPermissions`, `revokePermission`, `revokePermissions`, `hasPermission`), as permissions assignment is now managed via explicit `RolePermission` repository/service operations.

- [ ] **Step 2: Verify Compilation**

Run:
```bash
./mvnw clean compile -pl backend
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Role.java
git commit -m "refactor(auth): remove implicit @ManyToMany permissions from Role entity"
```

---

### Task 4: Refactor `PermissionSpecification` Subqueries for High Performance

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/spec/PermissionSpecification.java:54-87`

**Interfaces:**
- Consumes: `RolePermission` and `UserRole` `@EmbeddedId` fields.
- Produces: High-performance `Specification<Permission>` subqueries for `hasRoleId` and `hasUserId`.

- [ ] **Step 1: Rewrite `hasRoleId` and `hasUserId` in `PermissionSpecification.java`**

Replace `hasRoleId` and `hasUserId` in `PermissionSpecification.java`:

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
                  cb.isTrue(roleRoot.get("active")),
                  cb.isNull(roleRoot.get("deletedAt"))
              );
      return root.get("id").in(subquery);
    };
  }

  public static Specification<Permission> hasUserId(UUID userId) {
    return (root, query, cb) -> {
      if (userId == null) return cb.conjunction();
      Subquery<UUID> subquery = query.subquery(UUID.class);
      Root<RolePermission> rpRoot = subquery.from(RolePermission.class);
      Root<UserRole> userRoleRoot = subquery.from(UserRole.class);
      Root<Role> roleRoot = subquery.from(Role.class);
      subquery.select(rpRoot.get("id").get("permissionId"))
              .where(
                  cb.equal(userRoleRoot.get("id").get("userId"), userId),
                  cb.equal(userRoleRoot.get("id").get("roleId"), rpRoot.get("id").get("roleId")),
                  cb.equal(roleRoot.get("id"), rpRoot.get("id").get("roleId")),
                  cb.isTrue(roleRoot.get("active")),
                  cb.isNull(roleRoot.get("deletedAt"))
              );
      return root.get("id").in(subquery);
    };
  }
```

- [ ] **Step 2: Import `RolePermission` in `PermissionSpecification.java`**

Ensure `import c4f.vannang.vaops.modules.authorization.internal.domain.RolePermission;` is present at the top of `PermissionSpecification.java`.

- [ ] **Step 3: Verify Compilation & Run Tests**

Run:
```bash
./mvnw test -pl backend
```
Expected: BUILD SUCCESS and all authorization tests pass.

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/spec/PermissionSpecification.java
git commit -m "perf(auth): optimize PermissionSpecification subqueries using explicit RolePermission & UserRole embedded IDs"
```
