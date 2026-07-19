package c4f.vannang.vaops.modules.authentication.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.exception.AccountLockedException;
import c4f.vannang.vaops.modules.authentication.api.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderFactory;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderStrategy;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.infrastructure.crypto.Sha256DeterministicHashStrategy;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategyFactory;
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
  private TokenProviderFactory tokenProviderFactory;

  @Mock
  private TokenProviderStrategy tokenProviderStrategy;

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
        tokenProviderFactory,
        authProperties,
        refreshTokenWriteRepository,
        deterministicHashStrategyFactory);
  }

  @Test
  void execute_shouldLoginSuccessfully() {
    LoginCommandDto command = new LoginCommandDto(accountName, password);
    UserAuthDto userAuth = new UserAuthDto(userId, passwordHash, null, true);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(userAuth));

    when(passwordEncoder.matches(password, passwordHash)).thenReturn(true);
    when(tokenProviderFactory.getService(TokenType.JWT)).thenReturn(tokenProviderStrategy);
    when(tokenProviderStrategy.createAccessToken(any())).thenReturn("mock-access-token");
    when(tokenProviderStrategy.createRefreshToken(any())).thenReturn("mock-refresh-token");
    when(authProperties.getJwt()).thenReturn(jwtProperties);
    when(jwtProperties.getRefreshExpirationMs()).thenReturn(604800000L);

    LoginCommandResultDto result = loginUseCase.execute(command);

    assertNotNull(result);
    assertEquals("mock-access-token", result.accessToken());
    assertEquals("mock-refresh-token", result.refreshToken());

    verify(refreshTokenWriteRepository, times(1)).save(any(RefreshToken.class));
    verify(identityModuleApi, times(1)).recordSuccessfulLogin(new RecordSuccessfulLoginCommand(userId));
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_whenUserNotFound() {
    LoginCommandDto command = new LoginCommandDto(accountName, password);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class, () -> loginUseCase.execute(command));

    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
    verify(identityModuleApi, never()).recordFailedLogin(any());
  }

  @Test
  void execute_shouldThrowAccountLockedException_whenAccountLocked() {
    LoginCommandDto command = new LoginCommandDto(accountName, password);
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
    LoginCommandDto command = new LoginCommandDto(accountName, password);
    UserAuthDto inactiveUser = new UserAuthDto(userId, passwordHash, null, false);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(inactiveUser));

    assertThrows(UnauthenticatedException.class, () -> loginUseCase.execute(command));

    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_andRecordFailedLogin_whenPasswordIncorrect() {
    LoginCommandDto command = new LoginCommandDto(accountName, password);
    UserAuthDto userAuth = new UserAuthDto(userId, passwordHash, null, true);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenReturn(Optional.of(userAuth));
    when(passwordEncoder.matches(password, passwordHash)).thenReturn(false);

    assertThrows(UnauthenticatedException.class, () -> loginUseCase.execute(command));

    verify(identityModuleApi, times(1)).recordFailedLogin(new RecordFailedLoginCommand(accountName));
    verify(refreshTokenWriteRepository, never()).save(any());
    verify(identityModuleApi, never()).recordSuccessfulLogin(any());
  }

  @Test
  void execute_shouldWrapInInternalServerException_whenUnexpectedError() {
    LoginCommandDto command = new LoginCommandDto(accountName, password);

    when(identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName)))
        .thenThrow(new RuntimeException("Database connection failed"));

    InternalServerException exception = assertThrows(InternalServerException.class,
        () -> loginUseCase.execute(command));
    assertTrue(exception.getMessage().contains("Unexpected error"));
  }
}
