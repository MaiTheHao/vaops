# Authorization Module Usecases Design Specification

## Overview
This design covers the implementation of authorization management use cases within the `backend/src/main/java/c4f/vannang/vaops/modules/authorization` module of the Spring Boot 4.x application. 
The module handles Role-Based and Permission-Based Access Control (RBAC/PBAC) for users.

## Scope of Changes

### 1. Data Transfer Objects (DTO Records)
Location: `c4f.vannang.vaops.modules.authorization.internal.dto`

- **User-Role DTOs**:
  - `AssignRoleToUserCommand(UUID userId, Set<UUID> roleIds, UUID assignedBy)`
  - `RevokeRoleFromUserCommand(UUID userId, UUID roleId, UUID revokedBy)`
  - `GetUserRolesQuery(UUID userId)`
  - `UserRoleResponse(UUID userId, UUID roleId, String roleCode, Instant assignedAt, UUID assignedBy)`

- **Role-Permission DTOs**:
  - `AssignPermissionToRoleCommand(UUID roleId, Set<UUID> permissionIds, UUID updatedBy)`
  - `RevokePermissionFromRoleCommand(UUID roleId, UUID permissionId, UUID updatedBy)`
  - `GetUserPermissionsQuery(UUID userId)`
  - `CheckPermissionQuery(UUID userId, String resource, String action)`

- **Role CRUD DTOs**:
  - `CreateRoleCommand(String code, String description, Set<UUID> permissionIds, UUID createdBy)`
  - `UpdateRoleCommand(UUID id, String code, String description, Set<UUID> permissionIds, UUID updatedBy)`
  - `RoleResponse(UUID id, String code, String description, Boolean isActive, Instant createdAt, Instant updatedAt, Set<PermissionResponse> permissions)`

- **Permission CRUD DTOs**:
  - `CreatePermissionCommand(String resource, String action, String description, UUID createdBy)`
  - `UpdatePermissionCommand(UUID id, String resource, String action, String description, UUID updatedBy)`
  - `PermissionResponse(UUID id, String resource, String action, String description, Boolean isActive, Instant createdAt, Instant updatedAt)`

---

### 2. Detailed UseCase Implementations
Location: `c4f.vannang.vaops.modules.authorization.internal.usecase`

#### A. User - Role Assignment UseCases
- **`AssignRoleToUserUseCase`**:
  - Validate `userId` and input `roleIds`.
  - Fetch active roles via `RoleQueryRepository.findAllActiveByIds(roleIds)`. If any ID is invalid/inactive, throw `ResourceNotFoundException`.
  - For each `roleId`, inspect `UserRoleQueryRepository.findById(UserRoleId(userId, roleId))`:
    - **Existing & Revoked (`revokedAt != null`)**: Un-revoke by setting `revokedAt = null`, `revokedBy = null`, updating `assignedAt = Instant.now()`, `assignedBy = command.assignedBy()`.
    - **New**: Instantiate `UserRole` with `assignedAt = Instant.now()`, `assignedBy = command.assignedBy()`.
    - **Active**: Idempotently update `assignedAt`/`assignedBy` if needed.
  - Save all `UserRole` entities via `UserRoleWriteRepository.saveAll(...)`.

- **`RevokeRoleFromUserUseCase`**:
  - Find `UserRole` by `UserRoleId(command.userId(), command.roleId())`. Throw `ResourceNotFoundException` if missing.
  - If `revokedAt == null`, update `revokedAt = Instant.now()`, `revokedBy = command.revokedBy()`.
  - Save via `UserRoleWriteRepository.save(...)`.

- **`GetUserRolesUseCase`**:
  - Retrieve active roles using `RoleQueryRepository.findActiveRolesByUserId(userId)`.
  - Map to `List<RoleResponse>`.

#### B. Role - Permission & Permission Query UseCases
- **`AssignPermissionToRoleUseCase`**:
  - Find active role by `roleId` via `RoleQueryRepository.findActiveById(...)`. Throw `ResourceNotFoundException` if missing.
  - Find active permissions by IDs via `PermissionQueryRepository.findAllActiveByIds(...)`.
  - Add permissions to `role.getPermissions()`.
  - Save role via `RoleWriteRepository.save(role)`.

- **`RevokePermissionFromRoleUseCase`**:
  - Find active role by `roleId`.
  - Remove target permission from `role.getPermissions()`.
  - Save role via `RoleWriteRepository.save(role)`.

- **`GetUserPermissionsUseCase`**:
  - Query distinct active permissions via `PermissionQueryRepository.findActivePermissionsByUserId(userId)`.
  - Map to `List<PermissionResponse>`.

- **`CheckPermissionUseCase`**:
  - Query active permissions of `userId` via `PermissionQueryRepository.findActivePermissionsByUserId(userId)`.
  - Return `true` if any permission matches `resource` (case-insensitive or exact) and `action` (case-insensitive or exact).

#### C. Role CRUD UseCases
- **`CreateRoleUseCase`**:
  - Check uniqueness via `RoleQueryRepository.findByCode(command.code())`. Throw `ResourceAlreadyExistsException` if duplicate.
  - Fetch permissions if `permissionIds` provided.
  - Build `Role` entity, save via `RoleWriteRepository.save(...)`, return `RoleResponse`.

- **`UpdateRoleUseCase`**:
  - Find `Role` by ID via `RoleQueryRepository.findById(...)`. Throw `ResourceNotFoundException` if missing.
  - Check unique `code` collision if `code` was modified.
  - Update fields & permission set. Save via `RoleWriteRepository.save(...)`.

- **`SoftDeleteRoleUseCase`**:
  - Find `Role` by ID. Set `deletedAt = Instant.now()`, `deletedBy = operatorId`. Save via `RoleWriteRepository.save(...)`.

- **`GetRoleByIdUseCase` & `ListRolesUseCase`**:
  - Retrieve single active role or all active roles via `RoleQueryRepository`.

#### D. Permission CRUD UseCases
- **`CreatePermissionUseCase`**:
  - Check uniqueness via `PermissionQueryRepository.findByResourceAndAction(resource, action)`. Throw `ResourceAlreadyExistsException` if duplicate.
  - Build `Permission` entity, save via `PermissionWriteRepository.save(...)`, return `PermissionResponse`.

- **`UpdatePermissionUseCase`**:
  - Find `Permission` by ID. Check collision on `(resource, action)`. Update fields and save via `PermissionWriteRepository.save(...)`.

- **`SoftDeletePermissionUseCase`**:
  - Find `Permission` by ID. Set `deletedAt = Instant.now()`, `deletedBy = operatorId`. Save.

- **`GetPermissionByIdUseCase` & `ListPermissionsUseCase`**:
  - Retrieve active permissions via `PermissionQueryRepository`.

---

## Exception Mapping
- Resource missing -> `c4f.vannang.vaops.shared.exception.ResourceNotFoundException`
- Duplicate code or resource/action -> `c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException`
- Invalid arguments -> `c4f.vannang.vaops.shared.exception.ValidationException`

## Testing Strategy
- Unit testing is temporarily out of scope per user request for this phase. Verification will focus on clean compilation, repository query validations, and application build.
