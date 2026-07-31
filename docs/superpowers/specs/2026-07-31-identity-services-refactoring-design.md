# Identity Module Services Refactoring Design Spec

- **Date**: 2026-07-31
- **Topic**: Refactoring 12 Identity UseCases into Clean Domain Services (`UserService` & `UserProfileService`)
- **Module**: `c4f.vannang.vaops.modules.identity`

---

## 1. Context & Motivation

Currently, the `identity` module contains 12 fine-grained `UseCase` classes located in `c4f.vannang.vaops.modules.identity.internal.usecase`:
1. `ChangePasswordUseCase.java`
2. `CheckAvailableUserUseCase.java`
3. `FindUserByAccountNameUseCase.java`
4. `FindUserByIdUseCase.java`
5. `GetProfileUseCase.java`
6. `LoginFailedUseCase.java`
7. `LoginSuccessfulUseCase.java`
8. `RegisterUseCase.java`
9. `SearchUsersUseCase.java`
10. `SoftDeleteUseCase.java`
11. `ToggleStatusUseCase.java`
12. `UpdateProfileUseCase.java`

This high degree of fragmentation creates excessive file overhead and injection boilerplate in entry points like `IdentityModuleApiImpl` (which injects all 12 use cases) and `ProfileController` (which injects 4 separate use cases).

To align with module architecture patterns used elsewhere in the codebase (e.g., `authorization` module's `RoleService`), these 12 use cases will be combined into 2 clean, cohesive domain `@Service` classes.

---

## 2. Proposed Architecture & Services Breakdown

We create a new package `c4f.vannang.vaops.modules.identity.internal.service` containing two Spring `@Service` classes:

### 2.1. `UserProfileService`
Focuses on user self-service actions (profile retrieval, profile updates, password changes).

- **Annotations**: `@Service`, `@RequiredArgsConstructor`, `@Transactional`
- **Dependencies**: `UserQueryRepository`, `UserWriteRepository`, `PasswordEncoder`
- **Methods**:
  1. `getProfile(FindByIdCommand command)`: returns `User` (throws `ResourceNotFoundException` if absent).
  2. `updateProfile(UpdateProfileCommand command)`: returns updated `User`.
  3. `changePassword(ChangePasswordCommand command)`: validates password strength, checks old password, encodes new password, updates user.

### 2.2. `UserService`
Focuses on administrative user management, search, registration, account state, and login security audit records.

- **Annotations**: `@Service`, `@RequiredArgsConstructor`, `@Transactional`
- **Dependencies**: `UserQueryRepository`, `UserWriteRepository`, `PasswordEncoder`
- **Constants**: `MAX_FAILED_ATTEMPTS = 5`, `LOCK_DURATION = Duration.ofMinutes(15)`
- **Methods**:
  1. `register(RegisterCommand command)`: returns registered `User`.
  2. `searchUsers(UserSearchCriteria criteria)`: returns `Page<User>`.
  3. `findUserById(FindByIdCommand command)`: returns `Optional<User>`.
  4. `findUserByAccountName(FindByAccountNameCommand command)`: returns `Optional<User>`.
  5. `checkAvailableUser(CheckAvailableUserCommand command)`: void (validates user existence, active status, lock status).
  6. `softDelete(SoftDeleteUserCommand command)`: void.
  7. `toggleStatus(ToggleUserStatusCommand command)`: void.
  8. `recordSuccessfulLogin(RecordSuccessfulLoginCommand command)`: void.
  9. `recordFailedLogin(RecordFailedLoginCommand command)`: void.

---

## 3. Impacted Consumers & Entry Points

### 3.1. `ProfileController`
- Replaces imports & injections of `GetProfileUseCase`, `UpdateProfileUseCase`, `ChangePasswordUseCase`, `SoftDeleteUseCase` with `UserProfileService` and `UserService`.
- Endpoint mappings remain unchanged (`/api/v1/profile`).

### 3.2. `IdentityModuleApiImpl`
- Replaces imports & injections of 12 UseCases with `UserService` and `UserProfileService`.
- Delegates methods cleanly to the respective service calls.

### 3.3. Unit & Integration Tests
- Replace/consolidate individual UseCase test classes in `src/test/java/.../identity/internal/usecase/` into `UserServiceTest.java` and `UserProfileServiceTest.java` in `src/test/java/.../identity/internal/service/`.
- Update `ProfileControllerTest.java` to mock `UserProfileService` and `UserService`.

### 3.4. Deletion of Legacy Files
Once services and consumers are updated and tests pass, delete all 12 files under `c4f.vannang.vaops.modules.identity.internal.usecase`.

---

## 4. Verification Plan

1. **Compilation**: Run `./mvnw compile` to ensure no broken imports or references remain.
2. **Automated Unit & Integration Tests**: Run `./mvnw test` for the `identity` module to confirm all tests pass cleanly.
