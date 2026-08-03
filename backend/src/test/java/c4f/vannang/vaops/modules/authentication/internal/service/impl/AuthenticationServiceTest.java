package c4f.vannang.vaops.modules.authentication.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.authorization.api.service.AuthorizationAPIService;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategy;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.RefreshTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.feature.token.claims.RefreshTokenClaims;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  private static final String ACCOUNT_NAME = "john.doe";
  private static final String PASSWORD = "password123";
  private static final String DISPLAY_NAME = "John Doe";
  private static final String AVATAR_URL = "avatar.png";
  private static final String PASSWORD_HASH = "encoded-password";
  private static final String TOKEN_HASH = "token-hash";
  private static final long REFRESH_EXPIRATION_MS = 3_600_000L;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private IdentityUserAPIService identityUserService;

  @Mock
  private AccessTokenSpec accessTokenSpec;

  @Mock
  private RefreshTokenSpec refreshTokenSpec;

  @Mock
  private AuthProperties authProperties;

  @Mock
  private RefreshTokenQueryRepository refreshTokenQueryRepository;

  @Mock
  private RefreshTokenWriteRepository refreshTokenWriteRepository;

  @Mock
  private DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  @Mock
  private AuthorizationAPIService authorizationAPIService;

  @InjectMocks
  private AuthenticationServiceImpl authenticationService;

  @BeforeEach
  void setUp() {
    lenient().when(authorizationAPIService.getRolesByUserId(any(UUID.class))).thenReturn(List.of());
    lenient().when(authorizationAPIService.getPermissionsByUserId(any(UUID.class))).thenReturn(List.of());
  }

  // ---------------------------------------------------------------------
  // Helper methods
  // ---------------------------------------------------------------------

  private UserDto userDto(UUID id, boolean active) {
    return new UserDto(id, ACCOUNT_NAME, DISPLAY_NAME, AVATAR_URL, active, null, null, null);
  }

  private UserAuthDto userAuth(UUID id, Instant lockedUntil, boolean active) {
    return new UserAuthDto(id, PASSWORD_HASH, lockedUntil, active);
  }

  private RefreshToken validRefreshToken(UUID userId) {
    return RefreshToken.create(userId, TOKEN_HASH, Instant.now().plusSeconds(3600));
  }

  private RefreshToken expiredToken(UUID userId) {
    return RefreshToken.create(userId, TOKEN_HASH, Instant.now().minusSeconds(3600));
  }

  private RefreshToken revokedToken(UUID userId) {
    RefreshToken token = RefreshToken.create(userId, TOKEN_HASH, Instant.now().plusSeconds(3600));
    token.revoke();
    return token;
  }

  private void stubHashStrategy() {
    DeterministicHashStrategy strategy = new DeterministicHashStrategy() {
      @Override
      public String hash(String input) {
        return TOKEN_HASH;
      }

      @Override
      public DeterministicHashAlgorithm getAlgorithm() {
        return DeterministicHashAlgorithm.SHA_256;
      }
    };
    when(deterministicHashStrategyFactory.getStrategy(DeterministicHashAlgorithm.SHA_256))
        .thenReturn(strategy);
  }

  private void stubRefreshExpiration() {
    AuthProperties.Jwt jwt = new AuthProperties.Jwt();
    jwt.setRefreshExpirationMs(REFRESH_EXPIRATION_MS);
    when(authProperties.getJwt()).thenReturn(jwt);
  }

  // ---------------------------------------------------------------------------
  // register
  // ---------------------------------------------------------------------------

  @Nested
  class RegisterTests {

    @Test
    void register_ShouldReturnResultWithAllFields_WhenRegistrationSucceeds() {
      // given
      RegisterCommand command = new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL);
      UUID userId = UUID.randomUUID();
      UserDto registeredUser = userDto(userId, true);
      when(identityUserService.register(any(RegisterRequest.class))).thenReturn(registeredUser);

      // when
      RegisterCommandResult result = authenticationService.register(command);

      // then
      assertThat(result.id()).isEqualTo(userId);
      assertThat(result.accountName()).isEqualTo(ACCOUNT_NAME);
      assertThat(result.displayName()).isEqualTo(DISPLAY_NAME);
      assertThat(result.avatarUrl()).isEqualTo(AVATAR_URL);
    }

    @Test
    void register_ShouldPropagateValidationException_WhenIdentityServiceThrowsValidationException() {
      // given
      RegisterCommand command = new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL);
      when(identityUserService.register(any(RegisterRequest.class)))
          .thenThrow(new ValidationException("Account name is invalid"));

      // when / then
      assertThatThrownBy(() -> authenticationService.register(command))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Account name is invalid");
    }

    @Test
    void register_ShouldPropagateResourceAlreadyExistsException_WhenAccountAlreadyExists() {
      // given
      RegisterCommand command = new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL);
      when(identityUserService.register(any(RegisterRequest.class)))
          .thenThrow(new ResourceAlreadyExistsException("Account already exists"));

      // when / then
      assertThatThrownBy(() -> authenticationService.register(command))
          .isInstanceOf(ResourceAlreadyExistsException.class)
          .hasMessage("Account already exists");
    }

    @Test
    void register_ShouldPropagateResourceNotFoundException_WhenDefaultRoleIsMissing() {
      // given
      RegisterCommand command = new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL);
      when(identityUserService.register(any(RegisterRequest.class)))
          .thenThrow(new ResourceNotFoundException("Default role not found: USER"));

      // when / then
      assertThatThrownBy(() -> authenticationService.register(command))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage("Default role not found: USER");
    }

    @Test
    void register_ShouldWrapUnexpectedExceptionInInternalServerException_WhenIdentityServiceThrowsRuntimeException() {
      // given
      RegisterCommand command = new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL);
      IllegalStateException rootCause = new IllegalStateException("boom");
      when(identityUserService.register(any(RegisterRequest.class)))
          .thenThrow(rootCause);

      // when / then
      assertThatThrownBy(() -> authenticationService.register(command))
          .isInstanceOf(InternalServerException.class)
          .hasMessage("Unexpected error while registering. Please try again.")
          .hasCause(rootCause);
    }
  }

  // ---------------------------------------------------------------------------
  // login
  // ---------------------------------------------------------------------------

  @Nested
  class LoginTests {

    @Test
    void login_ShouldReturnTokensAndPersistRefreshToken_WhenCredentialsAreValid() {
      // given
      LoginCommand command = new LoginCommand(ACCOUNT_NAME, PASSWORD);
      UUID userId = UUID.randomUUID();
      UserAuthDto userAuth = userAuth(userId, null, true);
      when(identityUserService.getUserForAuth(new FindForAuthQuery(ACCOUNT_NAME)))
          .thenReturn(Optional.of(userAuth));
      when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
      when(accessTokenSpec.generate(new AccessTokenClaims(userId, ACCOUNT_NAME)))
          .thenReturn("access-token");
      when(refreshTokenSpec.generate(new RefreshTokenClaims(userId))).thenReturn("refresh-token");
      stubHashStrategy();
      stubRefreshExpiration();

      // when
      LoginCommandResult result = authenticationService.login(command);

      // then
      assertThat(result.accessToken()).isEqualTo("access-token");
      assertThat(result.refreshToken()).isEqualTo("refresh-token");
      verify(refreshTokenWriteRepository).save(any(RefreshToken.class));
      verify(identityUserService).recordSuccessfulLogin(new RecordSuccessfulLoginRequest(userId));
    }

    @Test
    void login_ShouldThrowUnauthenticatedExceptionAndRecordFailure_WhenPasswordDoesNotMatch() {
      // given
      LoginCommand command = new LoginCommand(ACCOUNT_NAME, "wrong-password");
      UUID userId = UUID.randomUUID();
      UserAuthDto userAuth = userAuth(userId, null, true);
      when(identityUserService.getUserForAuth(new FindForAuthQuery(ACCOUNT_NAME)))
          .thenReturn(Optional.of(userAuth));
      when(passwordEncoder.matches("wrong-password", PASSWORD_HASH)).thenReturn(false);

      // when / then
      assertThatThrownBy(() -> authenticationService.login(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("Invalid credentials");
      verify(identityUserService).recordFailedLogin(new RecordFailedLoginRequest(ACCOUNT_NAME));
    }

    @Test
    void login_ShouldThrowAccountLockedException_WhenAccountIsLocked() {
      // given
      LoginCommand command = new LoginCommand(ACCOUNT_NAME, PASSWORD);
      UUID userId = UUID.randomUUID();
      Instant lockedUntil = Instant.now().plusSeconds(3600);
      UserAuthDto userAuth = userAuth(userId, lockedUntil, true);
      when(identityUserService.getUserForAuth(new FindForAuthQuery(ACCOUNT_NAME)))
          .thenReturn(Optional.of(userAuth));

      // when / then
      assertThatThrownBy(() -> authenticationService.login(command))
          .isInstanceOf(AccountLockedException.class)
          .hasMessage("Account is locked until " + lockedUntil);
    }

    @Test
    void login_ShouldThrowUnauthenticatedException_WhenAccountIsInactive() {
      // given
      LoginCommand command = new LoginCommand(ACCOUNT_NAME, PASSWORD);
      UUID userId = UUID.randomUUID();
      UserAuthDto userAuth = userAuth(userId, null, false);
      when(identityUserService.getUserForAuth(new FindForAuthQuery(ACCOUNT_NAME)))
          .thenReturn(Optional.of(userAuth));

      // when / then
      assertThatThrownBy(() -> authenticationService.login(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("Account is deactivated");
    }

    @Test
    void login_ShouldWrapUnexpectedExceptionInInternalServerException_WhenIdentityServiceThrowsRuntimeException() {
      // given
      LoginCommand command = new LoginCommand(ACCOUNT_NAME, PASSWORD);
      when(identityUserService.getUserForAuth(new FindForAuthQuery(ACCOUNT_NAME)))
          .thenThrow(new IllegalStateException("boom"));

      // when / then
      assertThatThrownBy(() -> authenticationService.login(command))
          .isInstanceOf(InternalServerException.class)
          .hasMessage("Unexpected error while logging in. Please try again.");
    }
  }

  // ---------------------------------------------------------------------------
  // refreshToken
  // ---------------------------------------------------------------------------

  @Nested
  class RefreshTokenTests {

    @Test
    void refreshToken_ShouldRotateTokens_WhenTokenIsValid() {
      // given
      RefreshTokenCommand command = new RefreshTokenCommand("refresh-token");
      UUID userId = UUID.randomUUID();
      when(refreshTokenSpec.validate("refresh-token")).thenReturn(new RefreshTokenClaims(userId));
      stubHashStrategy();
      RefreshToken storedToken = validRefreshToken(userId);
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(storedToken));
      UserDto user = userDto(userId, true);
      when(identityUserService.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.of(user));
      when(accessTokenSpec.generate(new AccessTokenClaims(userId, ACCOUNT_NAME)))
          .thenReturn("new-access-token");
      when(refreshTokenSpec.generate(new RefreshTokenClaims(userId))).thenReturn("new-refresh-token");
      stubRefreshExpiration();

      // when
      RefreshTokenCommandResult result = authenticationService.refreshToken(command);

      // then
      assertThat(result.accessToken()).isEqualTo("new-access-token");
      assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
      assertThat(storedToken.isRevoked()).isTrue();
      verify(refreshTokenWriteRepository).saveAll(any(List.class));
    }

    @Test
    void refreshToken_ShouldThrowUnauthenticatedException_WhenTokenIsInvalid() {
      // given
      RefreshTokenCommand command = new RefreshTokenCommand("refresh-token");
      when(refreshTokenSpec.validate("refresh-token"))
          .thenReturn(new RefreshTokenClaims(UUID.randomUUID()));
      stubHashStrategy();
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

      // when / then
      assertThatThrownBy(() -> authenticationService.refreshToken(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("Invalid refresh token");
    }

    @Test
    void refreshToken_ShouldThrowUnauthenticatedException_WhenTokenIsExpired() {
      // given
      RefreshTokenCommand command = new RefreshTokenCommand("refresh-token");
      UUID userId = UUID.randomUUID();
      when(refreshTokenSpec.validate("refresh-token")).thenReturn(new RefreshTokenClaims(userId));
      stubHashStrategy();
      RefreshToken expiredToken = expiredToken(userId);
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(expiredToken));

      // when / then
      assertThatThrownBy(() -> authenticationService.refreshToken(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("Refresh token is expired");
    }

    @Test
    void refreshToken_ShouldRevokeActiveTokensAndThrowException_WhenTokenIsRevoked() {
      // given
      RefreshTokenCommand command = new RefreshTokenCommand("refresh-token");
      UUID userId = UUID.randomUUID();
      when(refreshTokenSpec.validate("refresh-token")).thenReturn(new RefreshTokenClaims(userId));
      stubHashStrategy();
      RefreshToken revokedToken = revokedToken(userId);
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(revokedToken));
      RefreshToken activeToken = validRefreshToken(userId);
      when(refreshTokenQueryRepository.findValidRefreshTokensByUserId(userId))
          .thenReturn(List.of(activeToken));

      // when / then
      assertThatThrownBy(() -> authenticationService.refreshToken(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("Refresh token has been revoked previously. Potential breach detected.");
      assertThat(activeToken.isRevoked()).isTrue();
      verify(refreshTokenWriteRepository).saveAll(any(List.class));
    }

    @Test
    void refreshToken_ShouldThrowUnauthenticatedException_WhenUserNotFound() {
      // given
      RefreshTokenCommand command = new RefreshTokenCommand("refresh-token");
      UUID userId = UUID.randomUUID();
      when(refreshTokenSpec.validate("refresh-token")).thenReturn(new RefreshTokenClaims(userId));
      stubHashStrategy();
      RefreshToken storedToken = validRefreshToken(userId);
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(storedToken));
      when(identityUserService.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.empty());

      // when / then
      assertThatThrownBy(() -> authenticationService.refreshToken(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("User not found");
    }

    @Test
    void refreshToken_ShouldThrowUnauthenticatedException_WhenUserIsInactive() {
      // given
      RefreshTokenCommand command = new RefreshTokenCommand("refresh-token");
      UUID userId = UUID.randomUUID();
      when(refreshTokenSpec.validate("refresh-token")).thenReturn(new RefreshTokenClaims(userId));
      stubHashStrategy();
      RefreshToken storedToken = validRefreshToken(userId);
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(storedToken));
      UserDto user = userDto(userId, false);
      when(identityUserService.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.of(user));

      // when / then
      assertThatThrownBy(() -> authenticationService.refreshToken(command))
          .isInstanceOf(UnauthenticatedException.class)
          .hasMessage("User account is inactive");
    }
  }

  // ---------------------------------------------------------------------------
  // logout
  // ---------------------------------------------------------------------------

  @Nested
  class LogoutTests {

    @Test
    void logout_ShouldRevokeTokenAndReturnTrue_WhenTokenIsFound() {
      // given
      LogoutCommand command = new LogoutCommand("refresh-token");
      stubHashStrategy();
      RefreshToken storedToken = validRefreshToken(UUID.randomUUID());
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(storedToken));

      // when
      LogoutCommandResult result = authenticationService.logout(command);

      // then
      assertThat(result.success()).isTrue();
      assertThat(storedToken.isRevoked()).isTrue();
      verify(refreshTokenWriteRepository).save(storedToken);
    }

    @Test
    void logout_ShouldReturnFalse_WhenTokenIsNotFound() {
      // given
      LogoutCommand command = new LogoutCommand("refresh-token");
      stubHashStrategy();
      when(refreshTokenQueryRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

      // when
      LogoutCommandResult result = authenticationService.logout(command);

      // then
      assertThat(result.success()).isFalse();
      verify(refreshTokenWriteRepository, never()).save(any(RefreshToken.class));
    }
  }
}