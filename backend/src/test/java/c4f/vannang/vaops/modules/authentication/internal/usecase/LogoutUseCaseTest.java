package c4f.vannang.vaops.modules.authentication.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.shared.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.crypto.Sha256DeterministicHashStrategy;

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

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

  @Mock
  private RefreshTokenQueryRepository queryRepository;

  @Mock
  private RefreshTokenWriteRepository writeRepository;

  private LogoutUseCase useCase;

  private DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  private final String rawToken = "refresh-token-value";

  @BeforeEach
  void setUp() {
    deterministicHashStrategyFactory =
        new DeterministicHashStrategyFactory(List.of(new Sha256DeterministicHashStrategy()));
    useCase = new LogoutUseCase(queryRepository, writeRepository, deterministicHashStrategyFactory);
  }

  @Test
  void execute_shouldRevokeTokenSuccessfully() {
    RefreshToken storedToken = RefreshToken.create(
        UUID.randomUUID(), "some-hash", Instant.now().plus(1, ChronoUnit.HOURS));

    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(storedToken));

    LogoutCommandResult result = useCase.execute(new LogoutCommand(rawToken));

    assertTrue(result.success());
    assertTrue(storedToken.isRevoked());
    verify(writeRepository).save(storedToken);
  }

  @Test
  void execute_shouldReturnFalse_whenTokenNotFound() {
    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    LogoutCommandResult result = useCase.execute(new LogoutCommand(rawToken));

    assertFalse(result.success());
    verify(writeRepository, never()).save(any());
  }

  @Test
  void execute_shouldReturnTrue_whenTokenAlreadyRevoked() {
    RefreshToken revokedToken = RefreshToken.create(
        UUID.randomUUID(), "some-hash", Instant.now().plus(1, ChronoUnit.HOURS));
    revokedToken.revoke();

    when(queryRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

    LogoutCommandResult result = useCase.execute(new LogoutCommand(rawToken));

    assertTrue(result.success());
    assertTrue(revokedToken.isRevoked());
    verify(writeRepository).save(revokedToken);
  }
}
