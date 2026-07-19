# AuthenticationController Dependency Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor `AuthenticationController` to depend only on the module's `internal` layer and remove dependencies on the public `api` package.

**Architecture:** We will replace the injection of `AuthenticationModuleApi` inside `AuthenticationController` with direct injections of the four individual use cases: `LoginUseCase`, `RegisterUseCase`, `RefreshTokenUseCase`, and `LogoutUseCase`. Mappings from incoming web DTOs to internal commands (and from internal command results to web responses/cookies) will be performed directly inside the controller.

**Tech Stack:** Java, Spring Boot, JUnit 5, Mockito.

## Global Constraints
- `AuthenticationController` must not import or depend on anything in `c4f.vannang.vaops.modules.authentication.api.*`.
- The controller must only depend on classes in `c4f.vannang.vaops.modules.authentication.internal.*` and `c4f.vannang.vaops.modules.authentication.infrastructure.web.*`.

---

### Task 1: Create Unit Test for AuthenticationController

**Files:**
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationControllerTest.java`

**Interfaces:**
- Consumes: `LoginUseCase`, `RegisterUseCase`, `RefreshTokenUseCase`, `LogoutUseCase`, `LoginWebRequestDto`, `RegisterWebRequestDto`, `RegisterWebResponseDto`
- Produces: Test verification for the controller's functionality.

- [ ] **Step 1: Write the unit test class with Mockito**
  Create the file `backend/src/test/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationControllerTest.java` with tests that mock the use cases and call the controller methods.

  ```java
  package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

  import static org.junit.jupiter.api.Assertions.*;
  import static org.mockito.Mockito.*;

  import c4f.vannang.vaops.core.env.AppProperties;
  import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.LoginWebRequestDto;
  import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebRequestDto;
  import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebResponseDto;
  import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
  import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties.Jwt;
  import c4f.vannang.vaops.modules.authentication.internal.dto.*;
  import c4f.vannang.vaops.modules.authentication.internal.usecase.*;
  import jakarta.servlet.http.Cookie;
  import jakarta.servlet.http.HttpServletRequest;
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
  class AuthenticationControllerTest {

      @Mock
      private LoginUseCase loginUseCase;
      @Mock
      private RegisterUseCase registerUseCase;
      @Mock
      private RefreshTokenUseCase refreshTokenUseCase;
      @Mock
      private LogoutUseCase logoutUseCase;
      @Mock
      private AppProperties appProperties;
      @Mock
      private AuthProperties authProperties;

      @InjectMocks
      private AuthenticationController controller;

      @BeforeEach
      void setUp() {
          Jwt jwt = new Jwt();
          jwt.setAccessExpirationMs(3600000L);
          jwt.setRefreshExpirationMs(86400000L);
          lenient().when(authProperties.getJwt()).thenReturn(jwt);
          lenient().when(appProperties.isProd()).thenReturn(false);
      }

      @Test
      void login_ShouldSetCookiesAndReturnOk() {
          LoginWebRequestDto webRequest = new LoginWebRequestDto("user", "password");
          LoginCommandResult commandResult = new LoginCommandResult("access-token-123", "refresh-token-123");

          when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(commandResult);

          ResponseEntity<Void> response = controller.login(webRequest);

          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertTrue(response.getHeaders().containsKey("Set-Cookie"));
          verify(loginUseCase).execute(new LoginCommand("user", "password"));
      }

      @Test
      void register_ShouldReturnCreatedWithBody() {
          RegisterWebRequestDto webRequest = new RegisterWebRequestDto("user", "password", "Display Name", "avatar-url");
          UUID generatedId = UUID.randomUUID();
          RegisterCommandResult commandResult = new RegisterCommandResult(generatedId, "user", "Display Name", "avatar-url");

          when(registerUseCase.execute(any(RegisterCommand.class))).thenReturn(commandResult);

          ResponseEntity<RegisterWebResponseDto> response = controller.register(webRequest);

          assertEquals(HttpStatus.CREATED, response.getStatusCode());
          assertNotNull(response.getBody());
          assertEquals(generatedId, response.getBody().id());
          assertEquals("user", response.getBody().accountName());
          assertEquals("Display Name", response.getBody().displayName());
          assertEquals("avatar-url", response.getBody().avatarUrl());
          verify(registerUseCase).execute(new RegisterCommand("user", "password", "Display Name", "avatar-url"));
      }

      @Test
      void refresh_ShouldSetCookiesAndReturnOk_WhenCookieExists() {
          HttpServletRequest request = mock(HttpServletRequest.class);
          Cookie cookie = new Cookie("refresh_token", "stored-refresh-token");
          when(request.getCookies()).thenReturn(new Cookie[]{cookie});

          RefreshTokenCommandResult commandResult = new RefreshTokenCommandResult("new-access-token", "new-refresh-token");
          when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class))).thenReturn(commandResult);

          ResponseEntity<Void> response = controller.refresh(request);

          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertTrue(response.getHeaders().containsKey("Set-Cookie"));
          verify(refreshTokenUseCase).execute(new RefreshTokenCommand("stored-refresh-token"));
      }

      @Test
      void logout_ShouldRevokeAndClearCookies() {
          HttpServletRequest request = mock(HttpServletRequest.class);
          Cookie cookie = new Cookie("refresh_token", "stored-refresh-token");
          when(request.getCookies()).thenReturn(new Cookie[]{cookie});

          ResponseEntity<Void> response = controller.logout(request);

          assertEquals(HttpStatus.OK, response.getStatusCode());
          assertTrue(response.getHeaders().containsKey("Set-Cookie"));
          verify(logoutUseCase).execute(new LogoutCommand("stored-refresh-token"));
      }
  }
  ```

- [ ] **Step 2: Run tests to verify the new test fails/compiles error**
  Run compilation and test in the backend directory.
  Run: `./mvnw test -pl backend -Dtest=AuthenticationControllerTest`
  Expected: Failure (Compilation error because `AuthenticationController` does not yet accept or contain the usecase fields, or has conflicting types).

- [ ] **Step 3: Force-add and commit the test file**
  Stage the new test file.
  Run: `git add -f backend/src/test/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationControllerTest.java`
  Run: `git commit -m "test: add unit test for AuthenticationController refactoring"`

---

### Task 2: Refactor AuthenticationController

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationController.java`

**Interfaces:**
- Consumes: `LoginUseCase`, `RegisterUseCase`, `RefreshTokenUseCase`, `LogoutUseCase`, `LoginWebRequestDto`, `RegisterWebRequestDto`, `RegisterWebResponseDto`
- Produces: Updated REST endpoints matching the design.

- [ ] **Step 1: Replace dependencies and refactor mapping code**
  Modify `/home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationController.java` with the new imports and direct use case executions.

  ```java
  package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

  import c4f.vannang.vaops.core.env.AppProperties;
  import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.LoginWebRequestDto;
  import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebRequestDto;
  import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebResponseDto;
  import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
  import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
  import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
  import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
  import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
  import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
  import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
  import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
  import c4f.vannang.vaops.modules.authentication.internal.exception.UnauthenticatedException;
  import c4f.vannang.vaops.modules.authentication.internal.usecase.LoginUseCase;
  import c4f.vannang.vaops.modules.authentication.internal.usecase.LogoutUseCase;
  import c4f.vannang.vaops.modules.authentication.internal.usecase.RefreshTokenUseCase;
  import c4f.vannang.vaops.modules.authentication.internal.usecase.RegisterUseCase;
  import jakarta.servlet.http.Cookie;
  import jakarta.servlet.http.HttpServletRequest;
  import jakarta.validation.Valid;
  import java.time.Duration;
  import lombok.RequiredArgsConstructor;
  import org.springframework.http.HttpHeaders;
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseCookie;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.PostMapping;
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.RequestMapping;
  import org.springframework.web.bind.annotation.RestController;

  @RestController
  @RequestMapping("/api/v1/auth")
  @RequiredArgsConstructor
  public class AuthenticationController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AppProperties appProperties;
    private final AuthProperties authProperties;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginWebRequestDto request) {
      LoginCommandResult result =
          loginUseCase.execute(new LoginCommand(request.accountName(), request.password()));

      ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
          .httpOnly(true)
          .secure(appProperties.isProd())
          .path("/")
          .maxAge(Duration.ofMillis(authProperties.getJwt().getAccessExpirationMs()))
          .sameSite("Lax")
          .build();

      ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
          .httpOnly(true)
          .secure(appProperties.isProd())
          .path("/")
          .maxAge(Duration.ofMillis(authProperties.getJwt().getRefreshExpirationMs()))
          .sameSite("Lax")
          .build();

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
          .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
          .build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterWebResponseDto> register(
        @Valid @RequestBody RegisterWebRequestDto request) {
      RegisterCommandResult result = registerUseCase.execute(new RegisterCommand(
          request.accountName(), request.password(), request.displayName(), request.avatarUrl()));

      RegisterWebResponseDto response = new RegisterWebResponseDto(
          result.id(), result.accountName(), result.displayName(), result.avatarUrl());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request) {
      String refreshTokenValue = extractRefreshTokenFromCookie(request);
      if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
        throw new UnauthenticatedException("Refresh token is missing");
      }

      RefreshTokenCommandResult result =
          refreshTokenUseCase.execute(new RefreshTokenCommand(refreshTokenValue));

      ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
          .httpOnly(true)
          .secure(appProperties.isProd())
          .path("/")
          .maxAge(Duration.ofMillis(authProperties.getJwt().getAccessExpirationMs()))
          .sameSite("Lax")
          .build();

      ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
          .httpOnly(true)
          .secure(appProperties.isProd())
          .path("/")
          .maxAge(Duration.ofMillis(authProperties.getJwt().getRefreshExpirationMs()))
          .sameSite("Lax")
          .build();

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
          .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
          .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
      String refreshTokenValue = extractRefreshTokenFromCookie(request);
      if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
        logoutUseCase.execute(new LogoutCommand(refreshTokenValue));
      }

      ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
          .httpOnly(true)
          .secure(appProperties.isProd())
          .path("/")
          .maxAge(0)
          .sameSite("Lax")
          .build();

      ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
          .httpOnly(true)
          .secure(appProperties.isProd())
          .path("/")
          .maxAge(0)
          .sameSite("Lax")
          .build();

      return ResponseEntity.ok()
          .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
          .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
          .build();
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
      if (request.getCookies() == null) return null;
      for (Cookie cookie : request.getCookies()) {
        if ("refresh_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
      return null;
    }
  }
  ```

- [ ] **Step 2: Run compiler and unit tests**
  Verify the changes compile and all tests pass.
  Run: `./mvnw clean test -pl backend`
  Expected: PASS

- [ ] **Step 3: Commit the controller refactoring**
  Stage the modified controller and commit.
  Run: `git add backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationController.java`
  Run: `git commit -m "refactor(auth): remove api package dependency from AuthenticationController"`
