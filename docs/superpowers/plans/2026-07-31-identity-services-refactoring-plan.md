# Identity Module Services Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor 12 fine-grained UseCase classes in `identity` module into 2 clean domain `@Service` classes (`UserProfileService` & `UserService`), update consumer classes, migrate tests, and delete legacy UseCase files.

**Architecture:** Create `UserProfileService` (user self-service actions) and `UserService` (account management & login state tracking) in `c4f.vannang.vaops.modules.identity.internal.service`. Update `ProfileController` and `IdentityModuleApiImpl` to depend on these two services, update unit test suites, and remove `c4f.vannang.vaops.modules.identity.internal.usecase`.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Data JPA, Lombok, JUnit 5, Mockito.

## Global Constraints

- Preserve all existing exception types and business validation rules verbatim.
- Maintain transactional boundaries (`@Transactional` and `@Transactional(readOnly = true)`).
- Preserve existing REST API contract in `ProfileController` and module interface contract in `IdentityModuleApi`.

---

### Task 1: Create `UserProfileService` and Unit Tests

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/service/UserProfileService.java`
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/identity/internal/service/UserProfileServiceTest.java`

**Interfaces:**
- Produces: `UserProfileService` with methods:
  - `User getProfile(FindByIdCommand command)`
  - `User updateProfile(UpdateProfileCommand command)`
  - `void changePassword(ChangePasswordCommand command)`

- [ ] **Step 1: Create `UserProfileService.java`**

```java
package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public User getProfile(FindByIdCommand command) {
    return userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  public User updateProfile(UpdateProfileCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    DisplayName dn = command.displayName() != null ? new DisplayName(command.displayName()) : null;
    AvatarUrl au = command.avatarUrl() != null ? new AvatarUrl(command.avatarUrl()) : null;

    user.updateProfile(dn, au);
    return userWriteRepository.save(user);
  }

  public void changePassword(ChangePasswordCommand command) {
    User.validatePasswordStrength(command.newPassword());

    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(command.oldPassword(), user.getPasswordHash().value())) {
      throw new BusinessRuleViolationException("Invalid old password");
    }

    user.changePassword(new PasswordHash(passwordEncoder.encode(command.newPassword())));
    userWriteRepository.save(user);
  }
}
```

- [ ] **Step 2: Create `UserProfileServiceTest.java`**

```java
package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName as TestDisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserProfileService userProfileService;

  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.register(
        new AccountName("john_doe"),
        new PasswordHash("encoded_pass"),
        new DisplayName("John Doe"),
        new AvatarUrl("http://avatar.com/john.png")
    );
  }

  @Test
  @TestDisplayName("getProfile should return user when user exists")
  void getProfile_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userProfileService.getProfile(new FindByIdCommand(userId));

    assertThat(result).isNotNull();
    assertThat(result.getAccountName().value()).isEqualTo("john_doe");
  }

  @Test
  @TestDisplayName("getProfile should throw ResourceNotFoundException when user does not exist")
  void getProfile_NotFound() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userProfileService.getProfile(new FindByIdCommand(userId)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  @TestDisplayName("updateProfile should update display name and avatar url")
  void updateProfile_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated = userProfileService.updateProfile(new UpdateProfileCommand(userId, "Jane Doe", "http://avatar.com/jane.png"));

    assertThat(updated.getDisplayName().value()).isEqualTo("Jane Doe");
    assertThat(updated.getAvatarUrl().value()).isEqualTo("http://avatar.com/jane.png");
  }

  @Test
  @TestDisplayName("changePassword should update password hash when old password matches")
  void changePassword_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("OldPass123!", "encoded_pass")).thenReturn(true);
    when(passwordEncoder.encode("NewPass123!")).thenReturn("new_encoded_pass");

    userProfileService.changePassword(new ChangePasswordCommand(userId, "OldPass123!", "NewPass123!"));

    verify(userWriteRepository).save(user);
    assertThat(user.getPasswordHash().value()).isEqualTo("new_encoded_pass");
  }

  @Test
  @TestDisplayName("changePassword should throw exception when old password does not match")
  void changePassword_InvalidOldPassword() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("WrongPass!", "encoded_pass")).thenReturn(false);

    assertThatThrownBy(() -> userProfileService.changePassword(new ChangePasswordCommand(userId, "WrongPass!", "NewPass123!")))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessage("Invalid old password");
  }
}
```

- [ ] **Step 3: Run unit test to verify `UserProfileServiceTest` passes**

Run: `./mvnw test -Dtest=UserProfileServiceTest`
Expected: BUILD SUCCESS (4/4 tests pass)

---

### Task 2: Create `UserService` and Unit Tests

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/service/UserService.java`
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/identity/internal/service/UserServiceTest.java`

**Interfaces:**
- Produces: `UserService` with methods:
  - `User register(RegisterCommand command)`
  - `Page<User> searchUsers(UserSearchCriteria criteria)`
  - `Optional<User> findUserById(FindByIdCommand command)`
  - `Optional<User> findUserByAccountName(FindByAccountNameCommand command)`
  - `void checkAvailableUser(CheckAvailableUserCommand command)`
  - `void softDelete(SoftDeleteUserCommand command)`
  - `void toggleStatus(ToggleUserStatusCommand command)`
  - `void recordSuccessfulLogin(RecordSuccessfulLoginCommand command)`
  - `void recordFailedLogin(RecordFailedLoginCommand command)`

- [ ] **Step 1: Create `UserService.java`**

```java
package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.*;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.spec.UserSpecification;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  public static final int MAX_FAILED_ATTEMPTS = 5;
  private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  public User register(RegisterCommand dto) {
    User.validatePasswordStrength(dto.rawPassword());

    AccountName accountName = new AccountName(dto.accountName());

    if (userQueryRepository.existsByAccountName(accountName)) {
      throw new ResourceAlreadyExistsException("Account name already exists");
    }

    String passwordHash = passwordEncoder.encode(dto.rawPassword());
    DisplayName displayName = dto.displayName() != null ? new DisplayName(dto.displayName()) : null;
    AvatarUrl avatarUrl = dto.avatarUrl() != null ? new AvatarUrl(dto.avatarUrl()) : null;

    User user = User.register(accountName, new PasswordHash(passwordHash), displayName, avatarUrl);

    return userWriteRepository.save(user);
  }

  @Transactional(readOnly = true)
  public Page<User> searchUsers(UserSearchCriteria criteria) {
    return userQueryRepository.findAll(
        UserSpecification.search(criteria),
        criteria.toPageable()
    );
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserById(FindByIdCommand command) {
    return userQueryRepository.findById(command.userId());
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserByAccountName(FindByAccountNameCommand command) {
    if (command.accountName() == null) return Optional.empty();
    return userQueryRepository.findByAccountName(new AccountName(command.accountName()));
  }

  @Transactional(readOnly = true)
  public void checkAvailableUser(CheckAvailableUserCommand command) {
    if (command == null || command.userId() == null) {
      throw new UnauthenticatedException("Invalid user identity");
    }

    User user = userQueryRepository
        .findById(command.userId())
        .orElseThrow(() -> new UnauthenticatedException("User not found: " + command.userId()));

    if (!user.isActive()) {
      throw new UnauthenticatedException("Account is deactivated");
    }

    if (user.isLocked()) {
      throw new AccountLockedException("Account is locked until " + user.getLockedUntil());
    }
  }

  public void softDelete(SoftDeleteUserCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found or already deleted"));

    user.softDelete(command.deletedBy());
    userWriteRepository.save(user);
  }

  public void toggleStatus(ToggleUserStatusCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (command.active()) {
      user.activate();
    } else {
      user.deactivate();
    }

    userWriteRepository.save(user);
  }

  public void recordSuccessfulLogin(RecordSuccessfulLoginCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.recordSuccessfulLogin();
    userWriteRepository.save(user);
  }

  public void recordFailedLogin(RecordFailedLoginCommand command) {
    if (command.accountName() == null) {
      throw new IllegalArgumentException("Account name must not be null");
    }
    User user = userQueryRepository.findByAccountName(new AccountName(command.accountName()))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.recordFailedLogin(MAX_FAILED_ATTEMPTS, LOCK_DURATION);
    userWriteRepository.save(user);
  }
}
```

- [ ] **Step 2: Create `UserServiceTest.java`**

```java
package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.*;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.register(
        new AccountName("john_doe"),
        new PasswordHash("encoded_pass"),
        null,
        null
    );
  }

  @Test
  @DisplayName("register should save new user when account name is available")
  void register_Success() {
    RegisterCommand cmd = new RegisterCommand("john_doe", "Pass123!", "John", null);
    when(userQueryRepository.existsByAccountName(any(AccountName.class))).thenReturn(false);
    when(passwordEncoder.encode("Pass123!")).thenReturn("encoded_pass");
    when(userWriteRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User registered = userService.register(cmd);

    assertThat(registered).isNotNull();
    assertThat(registered.getAccountName().value()).isEqualTo("john_doe");
  }

  @Test
  @DisplayName("register should throw ResourceAlreadyExistsException when account name exists")
  void register_AlreadyExists() {
    RegisterCommand cmd = new RegisterCommand("john_doe", "Pass123!", "John", null);
    when(userQueryRepository.existsByAccountName(any(AccountName.class))).thenReturn(true);

    assertThatThrownBy(() -> userService.register(cmd))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  @DisplayName("checkAvailableUser should succeed when user is active and unlocked")
  void checkAvailableUser_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.checkAvailableUser(new CheckAvailableUserCommand(userId));
  }

  @Test
  @DisplayName("checkAvailableUser should throw UnauthenticatedException when user not found")
  void checkAvailableUser_NotFound() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.checkAvailableUser(new CheckAvailableUserCommand(userId)))
        .isInstanceOf(UnauthenticatedException.class);
  }

  @Test
  @DisplayName("softDelete should soft delete user")
  void softDelete_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.softDelete(new SoftDeleteUserCommand(userId, userId));

    verify(userWriteRepository).save(user);
    assertThat(user.isDeleted()).isTrue();
  }

  @Test
  @DisplayName("toggleStatus should activate/deactivate user")
  void toggleStatus_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.toggleStatus(new ToggleUserStatusCommand(userId, false));
    assertThat(user.isActive()).isFalse();

    userService.toggleStatus(new ToggleUserStatusCommand(userId, true));
    assertThat(user.isActive()).isTrue();
  }

  @Test
  @DisplayName("recordSuccessfulLogin should reset login failures")
  void recordSuccessfulLogin_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.recordSuccessfulLogin(new RecordSuccessfulLoginCommand(userId));

    verify(userWriteRepository).save(user);
  }

  @Test
  @DisplayName("recordFailedLogin should record failed attempt")
  void recordFailedLogin_Success() {
    when(userQueryRepository.findByAccountName(any(AccountName.class))).thenReturn(Optional.of(user));

    userService.recordFailedLogin(new RecordFailedLoginCommand("john_doe"));

    verify(userWriteRepository).save(user);
  }
}
```

- [ ] **Step 3: Run unit test to verify `UserServiceTest` passes**

Run: `./mvnw test -Dtest=UserServiceTest`
Expected: BUILD SUCCESS

---

### Task 3: Refactor `ProfileController` and `IdentityModuleApiImpl`

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileController.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/IdentityModuleApiImpl.java`
- Modify: `backend/src/test/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileControllerTest.java`

- [ ] **Step 1: Update `ProfileController.java`**

Replace the 4 UseCase injections with `UserProfileService` and `UserService`:

```java
package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.PutUpdateProfileWebRequest;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ProfileWebResponse> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        User user = userProfileService.getProfile(new FindByIdCommand(principal.userId()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    public ResponseEntity<ProfileWebResponse> putUpdateProfile(
            @Valid @RequestBody PutUpdateProfileWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        User user = userProfileService.updateProfile(
            new UpdateProfileCommand(principal.userId(), request.displayName(), request.avatarUrl()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        userProfileService.changePassword(
            new ChangePasswordCommand(principal.userId(), request.oldPassword(), request.newPassword()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        userService.softDelete(new SoftDeleteUserCommand(principal.userId(), principal.userId()));
        return ResponseEntity.noContent().build();
    }

    private ProfileWebResponse toResponse(User user) {
        return new ProfileWebResponse(
            user.getId(),
            user.getAccountName() != null ? user.getAccountName().value() : null,
            user.getDisplayName() != null ? user.getDisplayName().value() : null,
            user.getAvatarUrl() != null ? user.getAvatarUrl().value() : null,
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
```

- [ ] **Step 2: Update `IdentityModuleApiImpl.java`**

Replace 12 UseCases with `UserService` and `UserProfileService`:

```java
package c4f.vannang.vaops.modules.identity.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByAccountNameQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.SoftDeleteUserRequest;
import c4f.vannang.vaops.modules.identity.api.dto.ToggleUserStatusRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.modules.identity.internal.dto.UserSearchCriteria;

import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class IdentityModuleApiImpl implements IdentityModuleApi {

  private final UserService userService;
  private final UserProfileService userProfileService;
  private final UserDtoMapper userDtoMapper;
  private final IdentityMapper identityMapper;

  @Override
  public Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query) {
    FindByAccountNameCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserByAccountName(internalQuery)
        .map(userDtoMapper::toAuthDto);
  }

  @Override
  public void checkAvailableUser(CheckAvailableUserQuery query) {
    CheckAvailableUserCommand internalQuery = identityMapper.toInternal(query);
    userService.checkAvailableUser(internalQuery);
  }

  @Override
  public void recordSuccessfulLogin(RecordSuccessfulLoginRequest command) {
    RecordSuccessfulLoginCommand internalCommand = identityMapper.toInternal(command);
    userService.recordSuccessfulLogin(internalCommand);
  }

  @Override
  public void recordFailedLogin(RecordFailedLoginRequest command) {
    RecordFailedLoginCommand internalCommand = identityMapper.toInternal(command);
    userService.recordFailedLogin(internalCommand);
  }

  @Override
  public UserDto register(RegisterRequest registerDto) {
    RegisterCommand internalCommand = identityMapper.toInternal(registerDto);
    return userDtoMapper.toDto(userService.register(internalCommand));
  }

  @Override
  public void softDelete(SoftDeleteUserRequest command) {
    SoftDeleteUserCommand internalCommand = identityMapper.toInternal(command);
    userService.softDelete(internalCommand);
  }

  @Override
  public void deactivate(ToggleUserStatusRequest command) {
    ToggleUserStatusCommand internalCommand = identityMapper.toInternal(command);
    userService.toggleStatus(internalCommand);
  }

  @Override
  public void activate(ToggleUserStatusRequest command) {
    ToggleUserStatusCommand internalCommand = identityMapper.toInternal(command);
    userService.toggleStatus(internalCommand);
  }

  @Override
  public void updateProfile(UpdateProfileRequest command) {
    UpdateProfileCommand internalCommand = identityMapper.toInternal(command);
    userProfileService.updateProfile(internalCommand);
  }

  @Override
  public void changePassword(ChangePasswordRequest command) {
    ChangePasswordCommand internalCommand = identityMapper.toInternal(command);
    userProfileService.changePassword(internalCommand);
  }

  @Override
  public Optional<UserDto> getUserById(FindByIdQuery query) {
    FindByIdCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserById(internalQuery)
        .map(userDtoMapper::toDto);
  }

  @Override
  public Optional<UserDto> findByAccountName(FindByAccountNameQuery query) {
    FindByAccountNameCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserByAccountName(internalQuery)
        .map(userDtoMapper::toDto);
  }

  @Override
  public PageResponse<UserDto> searchUsers(UserSearchCriteria criteria) {
    Page<User> userPage = userService.searchUsers(criteria);
    return PageResponse.from(userPage, userDtoMapper::toDto);
  }
}
```

- [ ] **Step 3: Update `ProfileControllerTest.java`**

Update mocks in `ProfileControllerTest.java` to use `UserProfileService` and `UserService` instead of individual UseCases.

- [ ] **Step 4: Run compilation and controller test**

Run: `./mvnw test-compile`
Run: `./mvnw test -Dtest=ProfileControllerTest`
Expected: BUILD SUCCESS

---

### Task 4: Cleanup Legacy UseCase Files & Old UseCase Tests

**Files:**
- Delete package: `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/usecase/` (12 `.java` files)
- Delete package: `backend/src/test/java/c4f/vannang/vaops/modules/identity/internal/usecase/` (11 `.java` test files)

- [ ] **Step 1: Remove 12 legacy UseCase classes**

Delete files under `backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/usecase/`.

- [ ] **Step 2: Remove 11 legacy UseCase test files**

Delete files under `backend/src/test/java/c4f/vannang/vaops/modules/identity/internal/usecase/`.

- [ ] **Step 3: Run full backend build and test suite**

Run: `./mvnw clean test`
Expected: BUILD SUCCESS (All tests pass cleanly)

- [ ] **Step 4: Commit changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/identity/
git add backend/src/test/java/c4f/vannang/vaops/modules/identity/
git commit -m "refactor(identity): merge 12 usecases into UserService and UserProfileService"
```
