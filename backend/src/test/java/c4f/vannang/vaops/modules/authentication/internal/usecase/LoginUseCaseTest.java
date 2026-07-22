package c4f.vannang.vaops.modules.authentication.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.shared.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.crypto.Sha256DeterministicHashStrategy;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.core.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import c4f.vannang.vaops.shared.token.specification.RefreshTokenSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private IdentityModuleApi identityModuleApi;

  @Mock
  private AccessTokenSpec accessTokenSpec;

  @Mock
  private RefreshTokenSpec refreshTokenSpec;

  @Mock
  private AuthProperties authProperties;

  @Mock
  private AuthProperties.Jwt jwtProperties;

  @Mock
  private RefreshTokenWriteRepository refreshTokenWriteRepository;

  private DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  private LoginUseCase loginUseCase;

  private final UUID userId = UUID.randomUUID();
  private final String accountName = "testuser";
  private final String password = "password123";
  private final String passwordHash = "encoded-password-hash";

  @BeforeEach
  void setUp() {
    deterministicHashStrategyFactory =
        new DeterministicHashStrategyFactory(List.of(new Sha256DeterministicHashStrategy()));

    loginUseCase = new LoginUseCase(
        passwordEncoder,
        identityModuleApi,
        accessTokenSpec,
        refreshTokenSpec,
        authProperties,
        refreshTokenWriteRepository,
        deterministicHashStrategyFactory);
  }

  @Test
  void execute_shouldLoginSuccessfully() {
    LoginCommand command = new LoginCommand(accountName, password);
    UserAuthDto userAuth = new UserAuthDto(userId, passwordHash, null, true);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(userAuth));

    when(passwordEncoder.matches(password, passwordHash)).thenReturn(true);
    when(accessTokenSpec.generate(any(AccessTokenClaims.class))).thenReturn("mock-access-token");
    when(refreshTokenSpec.generate(any(RefreshTokenClaims.class))).thenReturn("mock-refresh-token");
    when(authProperties.getJwt()).thenReturn(jwtProperties);
    when(jwtProperties.getRefreshExpirationMs()).thenReturn(604800000L);

    LoginCommandResult result = loginUseCase.execute(command);

    assertNotNull(result);
    assertEquals("mock-access-token", result.accessToken());
    assertEquals("mock-refresh-token", result.refreshToken());

    verify(refreshTokenWriteRepository, times(1)).save(any(RefreshToken.class));
    verify(identityModuleApi, times(1)).recordSuccessfulLogin(new RecordSuccessfulLoginRequest(userId));
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_whenUserNotFound() {
    LoginCommand command = new LoginCommand(accountName, password);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class, () -> loginUseCase.execute(command));

    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
    verify(identityModuleApi, never()).recordFailedLogin(any());
  }

  @Test
  void execute_shouldThrowAccountLockedException_whenAccountLocked() {
    LoginCommand command = new LoginCommand(accountName, password);
    Instant lockedUntil = Instant.now().plus(1, ChronoUnit.HOURS);
    UserAuthDto lockedUser = new UserAuthDto(userId, passwordHash, lockedUntil, true);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(lockedUser));

    assertThrows(AccountLockedException.class, () -> loginUseCase.execute(command));

    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_whenAccountDeactivated() {
    LoginCommand command = new LoginCommand(accountName, password);
    UserAuthDto inactiveUser = new UserAuthDto(userId, passwordHash, null, false);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(inactiveUser));

    assertThrows(UnauthenticatedException.class, () -> loginUseCase.execute(command));

    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_andRecordFailedLogin_whenPasswordIncorrect() {
    LoginCommand command = new LoginCommand(accountName, password);
    UserAuthDto userAuth = new UserAuthDto(userId, passwordHash, null, true);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(userAuth));
    when(passwordEncoder.matches(password, passwordHash)).thenReturn(false);

    assertThrows(UnauthenticatedException.class, () -> loginUseCase.execute(command));

    verify(identityModuleApi, times(1)).recordFailedLogin(new RecordFailedLoginRequest(accountName));
    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
  }

  @Test
  void execute_shouldWrapInInternalServerException_whenUnexpectedError() {
    LoginCommand command = new LoginCommand(accountName, password);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenThrow(new RuntimeException("Database connection failed"));

    InternalServerException exception = assertThrows(InternalServerException.class,
        () -> loginUseCase.execute(command));
    assertTrue(exception.getMessage().contains("Unexpected error"));
  }
}
