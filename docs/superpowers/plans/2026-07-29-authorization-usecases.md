# Authorization Module Usecases Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all backend authorization use cases including User-Role assignment/revocation/un-revocation, Role-Permission management, permission aggregation & check, and Role/Permission CRUD operations.

**Architecture:** Spring Boot 4.x modular monolith architecture following CQRS Lite pattern (separate Query and Write repositories) and Single Responsibility Principle UseCases.

**Tech Stack:** Java 21, Spring Boot 4.x, Spring Data JPA, Lombok.

## Global Constraints
- Backend root: `backend/src/main/java/c4f/vannang/vaops/modules/authorization`
- All DTOs implemented as Java `record`s in `c4f.vannang.vaops.modules.authorization.internal.dto`
- All UseCases implemented in `c4f.vannang.vaops.modules.authorization.internal.usecase`
- Use Spring `@Service` and `@Transactional` on UseCases
- Soft-revoke user roles via `revokedAt`/`revokedBy` instead of row deletion
- Re-activating revoked roles sets `revokedAt = null`, `revokedBy = null` and updates `assignedAt`/`assignedBy`
- Use existing exceptions from `c4f.vannang.vaops.shared.exception` (`ResourceNotFoundException`, `ResourceAlreadyExistsException`, `ValidationException`)
- Unit tests are temporarily skipped per user request; verification done via `./mvnw compile` in `backend` directory.

---

### Task 1: Create Authorization DTO Records

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/AssignRoleToUserCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/RevokeRoleFromUserCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/AssignPermissionToRoleCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/RevokePermissionFromRoleCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/CheckPermissionQuery.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/CreateRoleCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/UpdateRoleCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/RoleResponse.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/CreatePermissionCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/UpdatePermissionCommand.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/PermissionResponse.java`

**Interfaces:**
- Consumes: Java standard types (`UUID`, `Set`, `Instant`, `String`, `Boolean`)
- Produces: DTO records for commands, queries, and responses used by UseCases.

- [ ] **Step 1: Create User-Role DTO records**
  Create `AssignRoleToUserCommand(UUID userId, Set<UUID> roleIds, UUID assignedBy)` and `RevokeRoleFromUserCommand(UUID userId, UUID roleId, UUID revokedBy)`.

- [ ] **Step 2: Create Role-Permission DTO records**
  Create `AssignPermissionToRoleCommand(UUID roleId, Set<UUID> permissionIds, UUID updatedBy)`, `RevokePermissionFromRoleCommand(UUID roleId, UUID permissionId, UUID updatedBy)`, and `CheckPermissionQuery(UUID userId, String resource, String action)`.

- [ ] **Step 3: Create Role CRUD DTO records**
  Create `CreateRoleCommand(String code, String description, Set<UUID> permissionIds, UUID createdBy)`, `UpdateRoleCommand(UUID id, String code, String description, Set<UUID> permissionIds, UUID updatedBy)`, and `RoleResponse(UUID id, String code, String description, Boolean isActive, Instant createdAt, Instant updatedAt, Set<PermissionResponse> permissions)`.

- [ ] **Step 4: Create Permission CRUD DTO records**
  Create `CreatePermissionCommand(String resource, String action, String description, UUID createdBy)`, `UpdatePermissionCommand(UUID id, String resource, String action, String description, UUID updatedBy)`, and `PermissionResponse(UUID id, String resource, String action, String description, Boolean isActive, Instant createdAt, Instant updatedAt)`.

- [ ] **Step 5: Commit DTOs**
  ```bash
  git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/dto/*.java
  git commit -m "feat(authorization): add authorization DTO records"
  ```

---

### Task 2: Implement User-Role Management UseCases

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/AssignRoleToUserUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/RevokeRoleFromUserUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetUserRolesUseCase.java`

**Interfaces:**
- Consumes: `RoleQueryRepository`, `UserRoleQueryRepository`, `UserRoleWriteRepository`, `AssignRoleToUserCommand`, `RevokeRoleFromUserCommand`
- Produces: `AssignRoleToUserUseCase.execute(AssignRoleToUserCommand)`, `RevokeRoleFromUserUseCase.execute(RevokeRoleFromUserCommand)`, `GetUserRolesUseCase.execute(UUID userId)`

- [ ] **Step 1: Implement `AssignRoleToUserUseCase`**
  Validate input, fetch active roles via `roleQueryRepository.findAllActiveByIds(roleIds)`, inspect `userRoleQueryRepository.findById(new UserRoleId(userId, roleId))` for re-activation / un-revoking or creation, and save via `userRoleWriteRepository.saveAll(...)`.

- [ ] **Step 2: Implement `RevokeRoleFromUserUseCase`**
  Find `UserRole` by `UserRoleId(userId, roleId)`. If present and `revokedAt == null`, set `revokedAt = Instant.now()`, `revokedBy = command.revokedBy()` and save via `userRoleWriteRepository.save(...)`.

- [ ] **Step 3: Implement `GetUserRolesUseCase`**
  Fetch active roles for user via `roleQueryRepository.findActiveRolesByUserId(userId)` and map to `List<RoleResponse>`.

- [ ] **Step 4: Commit User-Role UseCases**
  ```bash
  git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/AssignRoleToUserUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/RevokeRoleFromUserUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetUserRolesUseCase.java
  git commit -m "feat(authorization): add user-role assignment and query usecases"
  ```

---

### Task 3: Implement Role-Permission & Permission Query UseCases

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/AssignPermissionToRoleUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/RevokePermissionFromRoleUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetUserPermissionsUseCase.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/CheckPermissionUseCase.java`

**Interfaces:**
- Consumes: `RoleQueryRepository`, `RoleWriteRepository`, `PermissionQueryRepository`, DTO commands and queries.
- Produces: `AssignPermissionToRoleUseCase`, `RevokePermissionFromRoleUseCase`, `GetUserPermissionsUseCase`, `CheckPermissionUseCase`.

- [ ] **Step 1: Implement `AssignPermissionToRoleUseCase`**
  Fetch active role by `roleId`, fetch active permissions by `permissionIds`, add to `role.getPermissions()`, and save via `roleWriteRepository.save(role)`.

- [ ] **Step 2: Implement `RevokePermissionFromRoleUseCase`**
  Fetch active role by `roleId`, remove target permission from `role.getPermissions()`, and save via `roleWriteRepository.save(role)`.

- [ ] **Step 3: Implement `GetUserPermissionsUseCase`**
  Call `permissionQueryRepository.findActivePermissionsByUserId(userId)` and map to `List<PermissionResponse>`.

- [ ] **Step 4: Implement `CheckPermissionUseCase`**
  Fetch active permissions for user via `permissionQueryRepository.findActivePermissionsByUserId(userId)` and return boolean matching `resource` and `action`.

- [ ] **Step 5: Commit Role-Permission UseCases**
  ```bash
  git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/AssignPermissionToRoleUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/RevokePermissionFromRoleUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetUserPermissionsUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/CheckPermissionUseCase.java
  git commit -m "feat(authorization): add role-permission assignment and permission checking usecases"
  ```

---

### Task 4: Implement Role CRUD UseCases

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/CreateRoleUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/UpdateRoleUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/SoftDeleteRoleUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetRoleByIdUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/ListRolesUseCase.java`

**Interfaces:**
- Consumes: `RoleQueryRepository`, `RoleWriteRepository`, `PermissionQueryRepository`, `CreateRoleCommand`, `UpdateRoleCommand`, `RoleResponse`.
- Produces: Role CRUD operations.

- [ ] **Step 1: Implement `CreateRoleUseCase`**
  Check duplicate code via `roleQueryRepository.findByCode(...)`. Build entity, assign initial permissions if provided, save via `roleWriteRepository.save(...)`.

- [ ] **Step 2: Implement `UpdateRoleUseCase`**
  Find role by ID, check duplicate code if updated, update fields and permissions, save via `roleWriteRepository.save(...)`.

- [ ] **Step 3: Implement `SoftDeleteRoleUseCase`**
  Find role by ID, update `deletedAt = Instant.now()`, `deletedBy = operatorId`, save via `roleWriteRepository.save(...)`.

- [ ] **Step 4: Implement `GetRoleByIdUseCase` & `ListRolesUseCase`**
  Implement single role query (`findActiveById`) and list query (`findAllActive`).

- [ ] **Step 5: Commit Role CRUD UseCases**
  ```bash
  git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/CreateRoleUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/UpdateRoleUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/SoftDeleteRoleUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetRoleByIdUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/ListRolesUseCase.java
  git commit -m "feat(authorization): add role CRUD usecases"
  ```

---

### Task 5: Implement Permission CRUD UseCases

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/CreatePermissionUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/UpdatePermissionUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/SoftDeletePermissionUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetPermissionByIdUseCase.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/ListPermissionsUseCase.java`

**Interfaces:**
- Consumes: `PermissionQueryRepository`, `PermissionWriteRepository`, `CreatePermissionCommand`, `UpdatePermissionCommand`, `PermissionResponse`.
- Produces: Permission CRUD operations.

- [ ] **Step 1: Implement `CreatePermissionUseCase`**
  Check duplicate resource+action via `permissionQueryRepository.findByResourceAndAction(...)`. Build entity, save via `permissionWriteRepository.save(...)`.

- [ ] **Step 2: Implement `UpdatePermissionUseCase`**
  Find permission by ID, check duplicate resource+action if changed, update fields, save via `permissionWriteRepository.save(...)`.

- [ ] **Step 3: Implement `SoftDeletePermissionUseCase`**
  Find permission by ID, set `deletedAt = Instant.now()`, `deletedBy = operatorId`, save via `permissionWriteRepository.save(...)`.

- [ ] **Step 4: Implement `GetPermissionByIdUseCase` & `ListPermissionsUseCase`**
  Implement single permission query (`findActiveById`) and list query (`findAllActive`).

- [ ] **Step 5: Commit Permission CRUD UseCases**
  ```bash
  git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/CreatePermissionUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/UpdatePermissionUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/SoftDeletePermissionUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/GetPermissionByIdUseCase.java \
          backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/usecase/ListPermissionsUseCase.java
  git commit -m "feat(authorization): add permission CRUD usecases"
  ```

---

### Task 6: Application Compilation & Build Verification

**Files:**
- None (Build verification task)

- [ ] **Step 1: Run Maven compilation**
  Run: `./mvnw compile` in `backend` directory.
  Expected: BUILD SUCCESS without compilation errors.

- [ ] **Step 2: Final git check**
  Run: `git status`
  Expected: Clean working tree.
