package c4f.vannang.vaops.modules.authentication.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.feature.crypto.Sha256DeterministicHashStrategy;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.RefreshTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.feature.token.claims.RefreshTokenClaims;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

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
  private AuthProperties.Jwt jwtProperties;

  @Mock
  private RefreshTokenQueryRepository queryRepository;

  @Mock
  private RefreshTokenWriteRepository writeRepository;

  private AuthenticationService service;

  private DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  private final UUID userId = UUID.randomUUID();
  private final String accountName = "testuser";
  private final String password = "password123";
  private final String passwordHash = "encoded-password-hash";
  private final String rawToken = "valid-refresh-jwt-token";

  @BeforeEach
  void setUp() {
    deterministicHashStrategyFactory =
        new DeterministicHashStrategyFactory(List.of(new Sha256DeterministicHashStrategy()));

    service = new AuthenticationService(
        passwordEncoder,
        identityUserService,
        accessTokenSpec,
        refreshTokenSpec,
        authProperties,
        queryRepository,
        writeRepository,
        deterministicHashStrategyFactory);
  }

  // --- login ---

  @Test
  void login_shouldLoginSuccessfully() {
    LoginCommand command = new LoginCommand(accountName, password);
    UserAuthDto userAuth = new UserAuthDto(userId, passwordHash, null, true);

    when(identityUserService.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(userAuth));

    when(passwordEncoder.matches(password, passwordHash)).thenReturn(true);
    when(accessTokenSpec.generate(any(AccessTokenClaims.class))).thenReturn("mock-access-token");
    when(refreshTokenSpec.generate(any(RefreshTokenClaims.class))).thenReturn("mock-refresh-token");
    when(authProperties.getJwt()).thenReturn(jwtProperties);
    when(jwtProperties.getRefreshExpirationMs()).thenReturn(604800000L);

    LoginCommandResult result = service.login(command);

    assertNotNull(result);
    assertEquals("mock-access-token", result.accessToken());
    assertEquals("mock-refresh-token", result.refreshToken());

    verify(writeRepository, times(1)).save(any(RefreshToken.class));
    verify(identityUserService, times(1)).recordSuccessfulLogin(new RecordSuccessfulLoginRequest(userId));
  }

  @Test
  void login_shouldThrowUnauthenticatedException_whenUserNotFound() {
    LoginCommand command = new LoginCommand(accountName, password);

    when(identityUserService.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class, () -> service.login(command));

    verify(writeRepository, never()).save(any());
    verify(identityUserService, never()).recordSuccessfulLogin(any());
    verify(identityUserService, never()).recordFailedLogin(any());
  }

  @Test
  void login_shouldThrowAccountLockedException_whenAccountLocked() {
    LoginCommand command = new LoginCommand(accountName, password);
    Instant lockedUntil = Instant.now().plus(1, ChronoUnit.HOURS);
    UserAuthDto lockedUser = new UserAuthDto(userId, passwordHash, lockedUntil, true);

    when(identityUserService.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(lockedUser));

    assertThrows(AccountLockedException.class, () -> service.login(command));

    verify(writeRepository, never()).save(any());
    verify(identityUserService, never()).recordSuccessfulLogin(any());
  }

  @Test
  void login_shouldThrowUnauthenticatedException_whenAccountDeactivated() {
    LoginCommand command = new LoginCommand(accountName, password);
    UserAuthDto inactiveUser = new UserAuthDto(userId, passwordHash, null, false);

    when(identityUserService.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(inactiveUser));

    assertThrows(UnauthenticatedException.class, () -> service.login(command));

    verify(writeRepository, never()).save(any());
    verify(identityUserService, never()).recordSuccessfulLogin(any());
  }

  @Test
  void login_shouldThrowUnauthenticatedException_andRecordFailedLogin_whenPasswordIncorrect() {
    LoginCommand command = new LoginCommand(accountName, password);
    UserAuthDto userAuth = new UserAuthDto(userId, passwordHash, null, true);

    when(identityUserService.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(userAuth));
    when(passwordEncoder.matches(password, passwordHash)).thenReturn(false);

    assertThrows(UnauthenticatedException.class, () -> service.login(command));

    verify(identityUserService, times(1)).recordFailedLogin(new RecordFailedLoginRequest(accountName));
    verify(writeRepository, never()).save(any());
    verify(identityUserService, never()).recordSuccessfulLogin(any());
  }

  @Test
  void login_shouldWrapInInternalServerException_whenUnexpectedError() {
    LoginCommand command = new LoginCommand(accountName, password);

    when(identityUserService.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenThrow(new RuntimeException("Database connection failed"));

    InternalServerException exception = assertThrows(InternalServerException.class,
        () -> service.login(command));
    assertTrue(exception.getMessage().contains("Unexpected error"));
  }

  // --- logout ---

  @Test
  void logout_shouldRevokeTokenSuccessfully() {
    RefreshToken storedToken = RefreshToken.create(
        UUID.randomUUID(), "some-hash", Instant.now().plus(1, ChronoUnit.HOURS));

    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

    LogoutCommandResult result = service.logout(new LogoutCommand(rawToken));

    assertTrue(result.success());
    assertTrue(storedToken.isRevoked());
    verify(writeRepository).save(storedToken);
  }

  @Test
  void logout_shouldReturnFalse_whenTokenNotFound() {
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    LogoutCommandResult result = service.logout(new LogoutCommand(rawToken));

    assertFalse(result.success());
    verify(writeRepository, never()).save(any());
  }

  @Test
  void logout_shouldReturnTrue_whenTokenAlreadyRevoked() {
    RefreshToken revokedToken = RefreshToken.create(
        UUID.randomUUID(), "some-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    revokedToken.revoke();

    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

    LogoutCommandResult result = service.logout(new LogoutCommand(rawToken));

    assertTrue(result.success());
    assertTrue(revokedToken.isRevoked());
    verify(writeRepository).save(revokedToken);
  }

  // --- refreshToken ---

  @Test
  void refreshToken_shouldRotateTokenSuccessfully() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken oldToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    UserDto activeUser = new UserDto(userId, "testuser", "Test User", null, true, null, null, null);

    when(authProperties.getJwt()).thenReturn(jwtProperties);
    when(jwtProperties.getRefreshExpirationMs()).thenReturn(604_800_000L);
    when(refreshTokenSpec.validate(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(oldToken));
    when(identityUserService.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.of(activeUser));
    when(accessTokenSpec.generate(any(AccessTokenClaims.class))).thenReturn("new-access-token");
    when(refreshTokenSpec.generate(any(RefreshTokenClaims.class))).thenReturn("new-refresh-token");

    RefreshTokenCommandResult result = service.refreshToken(new RefreshTokenCommand(rawToken));

    assertNotNull(result);
    assertEquals("new-access-token", result.accessToken());
    assertEquals("new-refresh-token", result.refreshToken());

    assertTrue(oldToken.isRevoked());
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Iterable<RefreshToken>> captor = ArgumentCaptor.forClass(Iterable.class);
    verify(writeRepository).saveAll(captor.capture());
    List<RefreshToken> savedTokens = (List<RefreshToken>) captor.getValue();
    assertEquals(2, savedTokens.size());
    assertEquals(oldToken, savedTokens.get(0));

    RefreshToken savedNewToken = savedTokens.get(1);
    assertNotNull(savedNewToken.getTokenHash());
    assertEquals(userId, savedNewToken.getUserId());
  }

  @Test
  void refreshToken_shouldThrowException_whenTokenNotFoundInDb() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    when(refreshTokenSpec.validate(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class,
        () -> service.refreshToken(new RefreshTokenCommand(rawToken)));
  }

  @Test
  void refreshToken_shouldThrowException_whenTokenIsExpired() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken expiredToken = RefreshToken.create(userId, "old-hash", Instant.now().minus(1, ChronoUnit.HOURS));

    when(refreshTokenSpec.validate(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

    assertThrows(UnauthenticatedException.class,
        () -> service.refreshToken(new RefreshTokenCommand(rawToken)));
  }

  @Test
  void refreshToken_shouldThrowException_whenTokenIsRevoked() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken revokedToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    revokedToken.revoke();

    when(refreshTokenSpec.validate(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

    assertThrows(UnauthenticatedException.class,
        () -> service.refreshToken(new RefreshTokenCommand(rawToken)));
  }

  @Test
  void refreshToken_shouldThrowException_whenUserNotFound() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken oldToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));

    when(refreshTokenSpec.validate(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(oldToken));
    when(identityUserService.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class,
        () -> service.refreshToken(new RefreshTokenCommand(rawToken)));
  }

  @Test
  void refreshToken_shouldThrowException_whenUserIsInactive() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken oldToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    UserDto inactiveUser = new UserDto(userId, "testuser", "Test User", null, false, null, null, null);

    when(refreshTokenSpec.validate(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(oldToken));
    when(identityUserService.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.of(inactiveUser));

    assertThrows(UnauthenticatedException.class,
        () -> service.refreshToken(new RefreshTokenCommand(rawToken)));
  }

  @Test
  void refreshToken_shouldThrowException_whenJwtIsInvalid() {
    when(refreshTokenSpec.validate(rawToken))
        .thenThrow(new UnauthenticatedException("Invalid token"));

    assertThrows(UnauthenticatedException.class,
        () -> service.refreshToken(new RefreshTokenCommand(rawToken)));
  }

  // --- register ---

  @Test
  void register_shouldRegisterSuccessfully() {
    RegisterCommand command = new RegisterCommand(
        "newuser", "password123", "New User", "http://avatar.url"
    );

    UserDto mockUserDto = new UserDto(
        userId, "newuser", "New User", "http://avatar.url", true, null, null, null
    );

    when(identityUserService.register(any(RegisterRequest.class))).thenReturn(mockUserDto);

    RegisterCommandResult result = service.register(command);

    assertNotNull(result);
    assertEquals(userId, result.id());
    assertEquals("newuser", result.accountName());
    assertEquals("New User", result.displayName());
    assertEquals("http://avatar.url", result.avatarUrl());

    verify(identityUserService, times(1)).register(any(RegisterRequest.class));
  }

  @Test
  void register_shouldThrowValidationException_whenIdentityApiThrowsIt() {
    RegisterCommand command = new RegisterCommand("user", "pass", "Name", null);

    when(identityUserService.register(any(RegisterRequest.class)))
        .thenThrow(new ValidationException("Invalid input"));

    assertThrows(ValidationException.class, () -> service.register(command));
  }

  @Test
  void register_shouldThrowResourceAlreadyExistsException_whenAccountExists() {
    RegisterCommand command = new RegisterCommand("existinguser", "pass", "Name", null);

    when(identityUserService.register(any(RegisterRequest.class)))
        .thenThrow(new ResourceAlreadyExistsException("User already exists"));

    assertThrows(ResourceAlreadyExistsException.class, () -> service.register(command));
  }

  @Test
  void register_shouldWrapInInternalServerException_whenUnexpectedError() {
    RegisterCommand command = new RegisterCommand("user", "pass", "Name", null);

    when(identityUserService.register(any(RegisterRequest.class)))
        .thenThrow(new RuntimeException("Database down"));

    InternalServerException exception = assertThrows(InternalServerException.class,
        () -> service.register(command));
    assertTrue(exception.getMessage().contains("Unexpected error"));
  }
}
