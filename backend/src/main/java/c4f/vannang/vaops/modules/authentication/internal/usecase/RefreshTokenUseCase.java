package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.dto.AccessTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderFactory;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderStrategy;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategyFactory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

  private final IdentityModuleApi identityModuleApi;
  private final TokenProviderFactory tokenServiceFactory;
  private final AuthProperties authProperties;
  private final RefreshTokenQueryRepository refreshTokenQueryRepository;
  private final RefreshTokenWriteRepository refreshTokenWriteRepository;
  private final DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  @Transactional
  public RefreshTokenCommandResultDto execute(RefreshTokenCommandDto command) {
    TokenProviderStrategy tokenService = tokenServiceFactory.getService(TokenType.JWT);
    RefreshTokenClaims claims = tokenService.validateRefreshToken(command.refreshToken());

    String tokenHash = deterministicHashStrategyFactory
        .getStrategy(DeterministicHashAlgorithm.SHA_256)
        .hash(command.refreshToken());

    RefreshToken storedToken = refreshTokenQueryRepository
        .findByTokenHash(tokenHash)
        .orElseThrow(() -> new UnauthenticatedException("Invalid refresh token"));

    if (!storedToken.isValid()) {
      throw new UnauthenticatedException("Invalid or revoked refresh token");
    }

    UserDto user = identityModuleApi
        .getUserById(new FindByIdQuery(claims.userId()))
        .orElseThrow(() -> new UnauthenticatedException("User not found"));
    if (!user.active()) {
      throw new UnauthenticatedException("User account is inactive");
    }

    storedToken.revoke();
    refreshTokenWriteRepository.save(storedToken);

    AccessTokenClaims accessClaims = new AccessTokenClaims(claims.userId(), user.accountName());
    RefreshTokenClaims refreshClaims = new RefreshTokenClaims(claims.userId());

    String newAccessToken = tokenService.createAccessToken(accessClaims);
    String newRefreshToken = tokenService.createRefreshToken(refreshClaims);

    String newTokenHash = deterministicHashStrategyFactory
        .getStrategy(DeterministicHashAlgorithm.SHA_256)
        .hash(newRefreshToken);
        
    Instant expiredAt = Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
    RefreshToken newEntity = RefreshToken.create(claims.userId(), newTokenHash, expiredAt);
    refreshTokenWriteRepository.save(newEntity);

    return new RefreshTokenCommandResultDto(newAccessToken, newRefreshToken);
  }
}
