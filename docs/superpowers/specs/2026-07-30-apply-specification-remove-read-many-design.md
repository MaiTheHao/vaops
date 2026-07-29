# Design Spec: Apply Specification & Remove Legacy READ-Many Methods

**Date:** 2026-07-30  
**Status:** Approved  
**Author:** Antigravity  

---

## 1. Overview & Goal

System queries currently retain legacy scattered READ-many query methods (e.g. `findAllActive()`, `findActivePermissionsByUserId()`, `findActiveRolesByUserId()`, `findAllByUserId()`, `findAllActiveByUserId()`, `findAllActiveByRoleId()`, `findAllByUserIdAndRoleIdIn()`). 

This specification establishes a clean, unified Specification-based query architecture by:
1. Retaining only single-entity lookups (`findById`, `findByCode`, etc.) and pure ID batch lookups (`findAllByIdIn`, `findAllActiveByIds`).
2. Eliminating ALL conditional scattered READ-many query methods across repository and service layers.
3. Expanding `PermissionSearchCriteria`, `RoleSearchCriteria`, `UserRoleSearchCriteria` and their corresponding Specifications (`PermissionSpecification`, `RoleSpecification`, `UserRoleSpecification`) to support all relational/conditional query parameters (`userId`, `roleId`, `roleIds`, `isRevoked`).
4. Removing legacy `list*()` and `getUser*()` methods from Service classes, making `search*()` the single standard entry point for querying multiple domain entities.

---

## 2. Detailed Scope & Component Changes

### 2.1. Authorization Module DTO & Specifications

#### `PermissionSearchCriteria.java`
- Add optional fields: `UUID userId`, `UUID roleId`.

#### `PermissionSpecification.java`
- Add static specification `hasRoleId(UUID roleId)`:
  `JOIN root.get("roles") r WHERE r.id = :roleId`
- Add static specification `hasUserId(UUID userId)`:
  `JOIN root.get("roles") r JOIN UserRole ur ON ur.id.roleId = r.id WHERE ur.id.userId = :userId AND ur.revokedAt IS NULL`
- Update composite `search(PermissionSearchCriteria criteria)` to combine `hasRoleId` and `hasUserId`.

#### `RoleSearchCriteria.java`
- Add optional field: `UUID userId`.

#### `RoleSpecification.java`
- Add static specification `hasUserId(UUID userId)`:
  `JOIN UserRole ur ON root.id = ur.id.roleId WHERE ur.id.userId = :userId AND ur.revokedAt IS NULL`
- Update composite `search(RoleSearchCriteria criteria)` to combine `hasUserId`.

#### `UserRoleSearchCriteria.java` (NEW)
- Define search criteria record for `UserRole`:
  - `UUID userId`, `UUID roleId`, `List<UUID> roleIds`, `Boolean isRevoked`
  - Pagination parameters: `int page`, `int size`, `String sortBy`, `String sortDirection`
  - Helper method: `toPageable()`

#### `UserRoleSpecification.java` (NEW)
- Define specifications for `UserRole`:
  - `hasUserId(UUID userId)`
  - `hasRoleId(UUID roleId)`
  - `hasRoleIdsIn(List<UUID> roleIds)`
  - `isNotRevoked()`
  - Master composite method `search(UserRoleSearchCriteria criteria)`

---

### 2.2. Repository Layer Changes

#### `PermissionQueryRepository.java`
- **REMOVE:**
  - `findAllActive()`
  - `findActivePermissionsByRoleId(UUID roleId)`
  - `findActivePermissionsByUserId(UUID userId)`
- **RETAIN:**
  - `findById`, `findActiveById`, `findByResourceAndAction`, `existsByResourceAndAction`, `findActiveByResourceAndAction`, `findAllByIdIn`, `findAllActiveByIds`, `hasPermission`.

#### `RoleQueryRepository.java`
- **REMOVE:**
  - `findAllActive()`
  - `findActiveRolesByUserId(UUID userId)`
- **RETAIN:**
  - `findById`, `findActiveById`, `findByCode`, `existsByCode`, `findActiveByCode`, `findAllByIdIn`, `findAllActiveByIds`.

#### `UserRoleQueryRepository.java`
- Change hierarchy to extend `BaseQueryRepository<UserRole, UserRoleId>` (exposing `JpaSpecificationExecutor<UserRole>`).
- **REMOVE:**
  - `findAllByUserId(UUID userId)`
  - `findAllActiveByUserId(UUID userId)`
  - `findAllActiveByRoleId(UUID roleId)`
  - `findAllByUserIdAndRoleIdIn(UUID userId, List<UUID> roleIds)`
- **RETAIN:**
  - `findById`, `existsActiveByUserIdAndRoleId`.

---

### 2.3. Service Layer Changes

#### `PermissionService.java`
- **REMOVE:**
  - `listPermissions()`
  - `getUserPermissions(UUID userId)`
- **RETAIN & USE:**
  - `searchPermissions(PermissionSearchCriteria criteria)`

#### `RoleService.java`
- **REMOVE:**
  - `listRoles()`
  - `getUserRoles(UUID userId)`
- **RETAIN & USE:**
  - `searchRoles(RoleSearchCriteria criteria)`

#### Internal Service Callers
- Update any internal calls in services (e.g. `UserRoleService`, `AssignRoleToUserCommand`, etc.) previously relying on deleted repository methods to use `search()` or `findAll(Specification)`.

---

## 3. Verification Plan

1. Compile the Java application using `./mvnw compile` to verify zero compilation errors.
2. Verify all references to deleted repository and service methods have been removed or replaced with Specification calls.
