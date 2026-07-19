package c4f.vannang.vaops.modules.authentication.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderFactory;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderStrategy;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.infrastructure.crypto.Sha256DeterministicHashStrategy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

  @Mock
  private RefreshTokenQueryRepository queryRepository;

  @Mock
  private RefreshTokenWriteRepository writeRepository;

  @Mock
  private TokenProviderFactory tokenProviderFactory;

  @Mock
  private TokenProviderStrategy tokenService;

  @Mock
  private IdentityModuleApi identityModuleApi;

  @Mock
  private AuthProperties authProperties;

  @Mock
  private AuthProperties.Jwt jwtProperties;

  @Captor
  private ArgumentCaptor<RefreshToken> tokenCaptor;

  private RefreshTokenUseCase useCase;

  private DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  private final UUID userId = UUID.randomUUID();
  private final String rawToken = "valid-refresh-jwt-token";

  @BeforeEach
  void setUp() {
    deterministicHashStrategyFactory = new DeterministicHashStrategyFactory(
        List.of(new Sha256DeterministicHashStrategy())
    );
    when(tokenProviderFactory.getService(TokenType.JWT)).thenReturn(tokenService);
    useCase = new RefreshTokenUseCase(
        identityModuleApi, tokenProviderFactory, authProperties,
        queryRepository, writeRepository, deterministicHashStrategyFactory);
  }

  @Test
  void execute_shouldRotateTokenSuccessfully() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken oldToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    UserDto activeUser = new UserDto(userId, "testuser", "Test User", null, true, null, null, null);

    when(authProperties.getJwt()).thenReturn(jwtProperties);
    when(jwtProperties.getRefreshExpirationMs()).thenReturn(604_800_000L);
    when(tokenService.validateRefreshToken(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(oldToken));
    when(identityModuleApi.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.of(activeUser));
    when(tokenService.createAccessToken(any())).thenReturn("new-access-token");
    when(tokenService.createRefreshToken(any())).thenReturn("new-refresh-token");

    RefreshTokenCommandResultDto result = useCase.execute(new RefreshTokenCommandDto(rawToken));

    assertNotNull(result);
    assertEquals("new-access-token", result.accessToken());
    assertEquals("new-refresh-token", result.refreshToken());

    assertTrue(oldToken.isRevoked());
    verify(writeRepository).save(oldToken);

    verify(writeRepository, times(2)).save(tokenCaptor.capture());
    RefreshToken savedNewToken = tokenCaptor.getAllValues().get(1);
    assertNotNull(savedNewToken.getTokenHash());
    assertEquals(userId, savedNewToken.getUserId());
  }

  @Test
  void execute_shouldThrowException_whenTokenNotFoundInDb() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    when(tokenService.validateRefreshToken(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class,
        () -> useCase.execute(new RefreshTokenCommandDto(rawToken)));
  }

  @Test
  void execute_shouldThrowException_whenTokenIsExpired() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken expiredToken = RefreshToken.create(userId, "old-hash", Instant.now().minus(1, ChronoUnit.HOURS));

    when(tokenService.validateRefreshToken(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

    assertThrows(UnauthenticatedException.class,
        () -> useCase.execute(new RefreshTokenCommandDto(rawToken)));
  }

  @Test
  void execute_shouldThrowException_whenTokenIsRevoked() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken revokedToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    revokedToken.revoke();

    when(tokenService.validateRefreshToken(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

    assertThrows(UnauthenticatedException.class,
        () -> useCase.execute(new RefreshTokenCommandDto(rawToken)));
  }

  @Test
  void execute_shouldThrowException_whenUserNotFound() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken oldToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));

    when(tokenService.validateRefreshToken(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(oldToken));
    when(identityModuleApi.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.empty());

    assertThrows(UnauthenticatedException.class,
        () -> useCase.execute(new RefreshTokenCommandDto(rawToken)));
  }

  @Test
  void execute_shouldThrowException_whenUserIsInactive() {
    RefreshTokenClaims claims = new RefreshTokenClaims(userId);
    RefreshToken oldToken = RefreshToken.create(userId, "old-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    UserDto inactiveUser = new UserDto(userId, "testuser", "Test User", null, false, null, null, null);

    when(tokenService.validateRefreshToken(rawToken)).thenReturn(claims);
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(oldToken));
    when(identityModuleApi.getUserById(new FindByIdQuery(userId))).thenReturn(Optional.of(inactiveUser));

    assertThrows(UnauthenticatedException.class,
        () -> useCase.execute(new RefreshTokenCommandDto(rawToken)));
  }

  @Test
  void execute_shouldThrowException_whenJwtIsInvalid() {
    when(tokenService.validateRefreshToken(rawToken))
        .thenThrow(new UnauthenticatedException("Invalid token"));

    assertThrows(UnauthenticatedException.class,
        () -> useCase.execute(new RefreshTokenCommandDto(rawToken)));
  }
}
