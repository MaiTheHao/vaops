# Profile Feature Endpoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create and secure the profile REST endpoints under the identity module, using direct injection of internal use cases.

**Architecture:** We will create DTOs in `infrastructure.web.dto` and the `ProfileController` in `infrastructure.web.controller` under the `identity` module. The controller will inject `FindUserByIdUseCase`, `UpdateProfileUseCase`, `ChangePasswordUseCase`, and `SoftDeleteUseCase`. The controller gets the authenticated user's ID via `principal.getName()`. We will also register `/api/v1/profile/**` as an authenticated route in `SecurityConfig.java`.

**Tech Stack:** Java, Spring Boot, JUnit 5, Mockito.

## Global Constraints
- `ProfileController` must not import or depend on anything in `c4f.vannang.vaops.modules.identity.api.*`.
- The controller must only depend on classes in `c4f.vannang.vaops.modules.identity.internal.*` and `c4f.vannang.vaops.modules.identity.infrastructure.web.*`.

---

### Task 1: Create Web DTOs

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/UpdateProfileWebRequest.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/ChangePasswordWebRequest.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/ProfileWebResponse.java`

- [ ] **Step 1: Create UpdateProfileWebRequest.java**
  ```java
  package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

  import jakarta.validation.constraints.NotBlank;

  public record UpdateProfileWebRequest(
      @NotBlank(message = "Display name is required")
      String displayName,
      String avatarUrl
  ) {}
  ```

- [ ] **Step 2: Create ChangePasswordWebRequest.java**
  ```java
  package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

  import jakarta.validation.constraints.NotBlank;
  import jakarta.validation.constraints.Size;

  public record ChangePasswordWebRequest(
      @NotBlank(message = "Old password is required")
      String oldPassword,
      @NotBlank(message = "New password is required")
      @Size(min = 8, message = "Password must be at least 8 characters")
      String newPassword
  ) {}
  ```

- [ ] **Step 3: Create ProfileWebResponse.java**
  ```java
  package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

  import java.time.Instant;
  import java.util.UUID;

  public record ProfileWebResponse(
      UUID id,
      String accountName,
      String displayName,
      String avatarUrl,
      Instant lastLoginAt,
      Instant createdAt,
      Instant updatedAt
  ) {}
  ```

- [ ] **Step 4: Run compiler to check syntax**
  Run: `./mvnw compile`
  Expected: BUILD SUCCESS

- [ ] **Step 5: Commit DTOs**
  Run: `git add backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/dto/*.java`
  Run: `git commit -m "feat(identity): add web request and response DTOs for profile feature"`

---

### Task 2: Create Unit Test for ProfileController

**Files:**
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileControllerTest.java`

**Interfaces:**
- Consumes: UseCases and DTOs from identity module.
- Produces: Test validation suite for the controller.

- [ ] **Step 1: Write ProfileControllerTest.java**
  Create the unit test utilizing Mockito.

  ```java
  package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

  import static org.junit.jupiter.api.Assertions.*;
  import static org.mockito.Mockito.*;

  import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
  import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
  import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UpdateProfileWebRequest;
  import c4f.vannang.vaops.modules.identity.internal.domain.User;
  import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
  import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
  import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
  import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
  import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
  import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
  import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
  import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
  import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
  import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserByIdUseCase;
  import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
  import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
  import java.security.Principal;
  import java.util.Optional;
  import java.util.UUID;
  import org.junit.jupiter.api.BeforeEach;
  import org.junit.jupiter.api.Test;
  import org.junit.jupiter.api.extension.ExtendWith;
  import org.mockito.InjectMocks;
  import org.mockito.Mock;
  import org.mockito.junit.jupiter.MockitoExtension;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;

  @ExtendWith(MockitoExtension.class)
  class ProfileControllerTest {

      @Mock
      private FindUserByIdUseCase findUserByIdUseCase;
      @Mock
      private UpdateProfileUseCase updateProfileUseCase;
      @Mock
      private ChangePasswordUseCase changePasswordUseCase;
      @Mock
      private SoftDeleteUseCase softDeleteUseCase;

      @InjectMocks
      private ProfileController controller;

      private UUID userId;
      private Principal principal;
      private User user;

      @BeforeEach
      void setUp() {
          userId = UUID.randomUUID();
          principal = mock(Principal.class);
          lenient().when(principal.getName()).thenReturn(userId.toString());

          user = User.register(
              new AccountName("test.user"),
              new PasswordHash("hashed-pw"),
              new DisplayName("Test User"),
              new AvatarUrl("http://avatar.url")
          );
          user.setId(userId);
      }

      @Test
      void getMyProfile_ShouldReturnProfile() {
          when(findUserByIdUseCase.execute(any(FindByIdCommand.class))).thenReturn(Optional.of(user));

          ResponseEntity<ProfileWebResponse> response = controller.getMyProfile(principal);

          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals(userId, response.getBody().id());
          assertEquals("test.user", response.getBody().accountName());
          assertEquals("Test User", response.getBody().displayName());
          assertEquals("http://avatar.url", response.getBody().avatarUrl());
          verify(findUserByIdUseCase).execute(new FindByIdCommand(userId));
      }

      @Test
      void updateProfile_ShouldExecuteUpdateAndReturnUpdatedProfile() {
          UpdateProfileWebRequest webRequest = new UpdateProfileWebRequest("New Name", "http://new.avatar");
          
          user.updateProfile(new DisplayName("New Name"), new AvatarUrl("http://new.avatar"));
          when(findUserByIdUseCase.execute(any(FindByIdCommand.class))).thenReturn(Optional.of(user));

          ResponseEntity<ProfileWebResponse> response = controller.updateProfile(webRequest, principal);

          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals("New Name", response.getBody().displayName());
          assertEquals("http://new.avatar", response.getBody().avatarUrl());
          verify(updateProfileUseCase).execute(new UpdateProfileCommand(userId, "New Name", "http://new.avatar"));
      }

      @Test
      void changePassword_ShouldExecuteChangeAndReturnOk() {
          ChangePasswordWebRequest webRequest = new ChangePasswordWebRequest("old-password", "new-password");

          ResponseEntity<Void> response = controller.changePassword(webRequest, principal);

          assertEquals(HttpStatus.OK, response.getStatusCode());
          verify(changePasswordUseCase).execute(new ChangePasswordCommand(userId, "old-password", "new-password"));
      }

      @Test
      void deleteAccount_ShouldExecuteSoftDeleteAndReturnNoContent() {
          ResponseEntity<Void> response = controller.deleteAccount(principal);

          assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
          verify(softDeleteUseCase).execute(new SoftDeleteUserCommand(userId, userId));
      }
  }
  ```

- [ ] **Step 2: Run tests to verify the test fails compilation**
  Run: `./mvnw test -Dtest=ProfileControllerTest`
  Expected: BUILD FAILURE (Compilation failure because `ProfileController` doesn't exist yet)

- [ ] **Step 3: Force-add and commit test file**
  Run: `git add -f backend/src/test/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileControllerTest.java`
  Run: `git commit -m "test: add unit test class for ProfileController"`

---

### Task 3: Create ProfileController and Update Security Config

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileController.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/core/config/SecurityConfig.java:35-44`

- [ ] **Step 1: Create ProfileController.java**
  Create the controller class body injecting the use cases and mapping the endpoints directly to use cases.

  ```java
  package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

  import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
  import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
  import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UpdateProfileWebRequest;
  import c4f.vannang.vaops.modules.identity.internal.domain.User;
  import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
  import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
  import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
  import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
  import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
  import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserByIdUseCase;
  import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
  import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
  import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
  import jakarta.validation.Valid;
  import java.security.Principal;
  import java.util.UUID;
  import lombok.RequiredArgsConstructor;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.*;

  @RestController
  @RequestMapping("/api/v1/profile")
  @RequiredArgsConstructor
  public class ProfileController {

      private final FindUserByIdUseCase findUserByIdUseCase;
      private final UpdateProfileUseCase updateProfileUseCase;
      private final ChangePasswordUseCase changePasswordUseCase;
      private final SoftDeleteUseCase softDeleteUseCase;

      @GetMapping
      public ResponseEntity<ProfileWebResponse> getMyProfile(Principal principal) {
          UUID userId = UUID.fromString(principal.getName());
          User user = findUserByIdUseCase.execute(new FindByIdCommand(userId))
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));
          return ResponseEntity.ok(toResponse(user));
      }

      @PutMapping
      public ResponseEntity<ProfileWebResponse> updateProfile(
              @Valid @RequestBody UpdateProfileWebRequest request,
              Principal principal) {
          UUID userId = UUID.fromString(principal.getName());
          updateProfileUseCase.execute(new UpdateProfileCommand(userId, request.displayName(), request.avatarUrl()));
          User user = findUserByIdUseCase.execute(new FindByIdCommand(userId))
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));
          return ResponseEntity.ok(toResponse(user));
      }

      @PutMapping("/password")
      public ResponseEntity<Void> changePassword(
              @Valid @RequestBody ChangePasswordWebRequest request,
              Principal principal) {
          UUID userId = UUID.fromString(principal.getName());
          changePasswordUseCase.execute(new ChangePasswordCommand(userId, request.oldPassword(), request.newPassword()));
          return ResponseEntity.ok().build();
      }

      @DeleteMapping
      public ResponseEntity<Void> deleteAccount(Principal principal) {
          UUID userId = UUID.fromString(principal.getName());
          softDeleteUseCase.execute(new SoftDeleteUserCommand(userId, userId));
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

- [ ] **Step 2: Modify SecurityConfig.java**
  Add the security rule matching `/api/v1/profile/**` requiring it to be authenticated.
  Specifically, inside the `securityFilterChain` method:

  ```java
        .authorizeHttpRequests(auth -> auth.requestMatchers("/hello", "/api/v1/hello")
            .permitAll()
            .requestMatchers("/api/v1/auth/login")
            .permitAll()
            .requestMatchers("/api/v1/auth/register")
            .permitAll()
            .requestMatchers("/api/v1/auth/refresh")
            .permitAll()
            .requestMatchers("/api/v1/profile/**")
            .authenticated()
            .anyRequest()
            .authenticated())
  ```

- [ ] **Step 3: Run all unit tests**
  Run: `./mvnw test`
  Expected: BUILD SUCCESS (All tests pass)

- [ ] **Step 4: Commit changes**
  Run: `git add backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileController.java`
  Run: `git add backend/src/main/java/c4f/vannang/vaops/core/config/SecurityConfig.java`
  Run: `git commit -m "feat(identity): implement ProfileController and secure profile endpoints"`
