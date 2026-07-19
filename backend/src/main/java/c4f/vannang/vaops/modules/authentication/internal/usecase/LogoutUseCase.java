package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.modules.authentication.api.dto.LogoutCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutCommandResultDto;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategyFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutUseCase {

  private final RefreshTokenQueryRepository refreshTokenQueryRepository;
  private final RefreshTokenWriteRepository refreshTokenWriteRepository;
  private final DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  public LogoutCommandResultDto execute(LogoutCommandDto command) {
    String tokenHash = deterministicHashStrategyFactory
        .getStrategy(DeterministicHashAlgorithm.SHA_256)
        .hash(command.refreshToken());

    Optional<RefreshToken> storedToken = refreshTokenQueryRepository.findByTokenHash(tokenHash);

    if (storedToken.isPresent()) {
      storedToken.get().revoke();
      refreshTokenWriteRepository.save(storedToken.get());
      return new LogoutCommandResultDto(true);
    }

    return new LogoutCommandResultDto(false);
  }
}
