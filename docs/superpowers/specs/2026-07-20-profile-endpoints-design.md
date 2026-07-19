# Profile Feature Endpoints Design

Create and implement the REST endpoints for user profile management under the `identity` module.

## Context & Motivation
Users need the ability to retrieve their profile details, update display name/avatar, change password, and delete their own account. 
To follow proper architecture guidelines, the `ProfileController` (residing in the `identity` module's web infrastructure layer) should not depend on the public API interface (`IdentityModuleApi`). It must directly inject internal UseCases.

Security details:
- Standard Spring Security principal represents the authenticated user where `principal.getName()` is the user ID in UUID string format.
- Authenticated requests are intercepted and verified via the existing `AuthenticationFilter`.

## Design Details

### 1. New DTOs in `c4f.vannang.vaops.modules.identity.infrastructure.web.dto`
- **`UpdateProfileWebRequest`**: Contains `displayName` and `avatarUrl` fields with validation annotation `@NotBlank` on `displayName`.
- **`ChangePasswordWebRequest`**: Contains `oldPassword` and `newPassword` (size min 8) fields.
- **`ProfileWebResponse`**: Contains user info including `id`, `accountName`, `displayName`, `avatarUrl`, `lastLoginAt`, `createdAt`, and `updatedAt`.

### 2. Controller in `c4f.vannang.vaops.modules.identity.infrastructure.web.controller`
- **`ProfileController`**:
  Injects:
  - `FindUserByIdUseCase`
  - `UpdateProfileUseCase`
  - `ChangePasswordUseCase`
  - `SoftDeleteUseCase`

### 3. Security Rules in `SecurityConfig.java`
Allow authenticated access to `/api/v1/profile/**`.

## Proposed Changes

#### [UpdateProfileWebRequest.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/UpdateProfileWebRequest.java) [NEW]
#### [ChangePasswordWebRequest.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/ChangePasswordWebRequest.java) [NEW]
#### [ProfileWebResponse.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/ProfileWebResponse.java) [NEW]
#### [ProfileController.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileController.java) [NEW]
#### [SecurityConfig.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/core/config/SecurityConfig.java) [MODIFY]

## Verification Plan

### Automated Tests
- Create unit tests for `ProfileController` verifying all REST endpoints.
- Run `./mvnw clean test` to compile and pass all tests.
