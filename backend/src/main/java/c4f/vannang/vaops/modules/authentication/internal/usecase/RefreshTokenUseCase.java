package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.TokenProviderFactory;
import c4f.vannang.vaops.modules.authentication.internal.TokenProviderStrategy;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.dto.AccessTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategy;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategyFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
  public RefreshTokenCommandResult execute(RefreshTokenCommand command) {
    TokenProviderStrategy tokenService = tokenServiceFactory.getService(TokenType.JWT);
    RefreshTokenClaims claims = tokenService.validateRefreshToken(command.refreshToken());
    DeterministicHashStrategy hashStrategy =
        deterministicHashStrategyFactory.getStrategy(DeterministicHashAlgorithm.SHA_256);

    String tokenHash = hashStrategy.hash(command.refreshToken());
    RefreshToken storedToken = refreshTokenQueryRepository
        .findByTokenHash(tokenHash)
        .orElseThrow(() -> new UnauthenticatedException("Invalid refresh token"));

    if (storedToken.isExpired()) throw new UnauthenticatedException("Refresh token is expired");
    if (storedToken.isRevoked()) {
      List<RefreshToken> activeTokens =
          refreshTokenQueryRepository.findValidRefreshTokensByUserId(claims.userId());
      activeTokens.forEach(refreshToken -> refreshToken.revoke());
      refreshTokenWriteRepository.saveAll(activeTokens);

      throw new UnauthenticatedException(
          "Refresh token has been revoked previously. Potential breach detected.");
    }

    UserDto user = identityModuleApi
        .getUserById(new FindByIdQuery(claims.userId()))
        .orElseThrow(() -> new UnauthenticatedException("User not found"));

    if (!user.active()) throw new UnauthenticatedException("User account is inactive");

    List<RefreshToken> tokensToSave = new ArrayList<>();
    storedToken.revoke();
    tokensToSave.add(storedToken);

    AccessTokenClaims accessClaims = new AccessTokenClaims(claims.userId(), user.accountName());
    RefreshTokenClaims refreshClaims = new RefreshTokenClaims(claims.userId());
    String newAccessToken = tokenService.createAccessToken(accessClaims);
    String newRefreshToken = tokenService.createRefreshToken(refreshClaims);
    String newTokenHash = hashStrategy.hash(newRefreshToken);

    Instant expiredAt = Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
    RefreshToken newEntity = RefreshToken.create(claims.userId(), newTokenHash, expiredAt);
    tokensToSave.add(newEntity);
    refreshTokenWriteRepository.saveAll(tokensToSave);

    return new RefreshTokenCommandResult(newAccessToken, newRefreshToken);
  }
}
