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
import c4f.vannang.vaops.modules.authentication.internal.util.TokenHashUtil;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final IdentityModuleApi identityModuleApi;
    private final TokenProviderFactory tokenServiceFactory;
    private final AuthProperties authProperties;
    private final RefreshTokenQueryRepository refreshTokenQueryRepository;
    private final RefreshTokenWriteRepository refreshTokenWriteRepository;

    @Transactional
    public RefreshTokenCommandResultDto execute(RefreshTokenCommandDto command) {
        // 1. Validate JWT refresh token structure and signature
        TokenProviderStrategy tokenService = tokenServiceFactory.getService(TokenType.JWT);
        RefreshTokenClaims claims = tokenService.validateRefreshToken(command.refreshToken());

        // 2. Compute SHA-256 hash of the raw token
        String tokenHash = TokenHashUtil.hash(command.refreshToken());

        // 3. Fetch the stored refresh token by hash
        RefreshToken storedToken = refreshTokenQueryRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthenticatedException("Invalid refresh token"));

        // 4. Verify token is still valid (not expired, not revoked)
        if (!storedToken.isValid()) {
            throw new UnauthenticatedException("Invalid or revoked refresh token");
        }

        // 5. Check user account status
        UserDto user = identityModuleApi.getUserById(new FindByIdQuery(claims.userId()))
                .orElseThrow(() -> new UnauthenticatedException("User not found"));
        if (!user.active()) {
            throw new UnauthenticatedException("User account is inactive");
        }

        // 6. Apply token rotation
        // 6a. Revoke the old token
        storedToken.revoke();
        refreshTokenWriteRepository.save(storedToken);

        // 6b. Generate new access and refresh tokens
        AccessTokenClaims accessClaims = new AccessTokenClaims(claims.userId(), user.accountName());
        RefreshTokenClaims refreshClaims = new RefreshTokenClaims(claims.userId());

        String newAccessToken = tokenService.createAccessToken(accessClaims);
        String newRefreshToken = tokenService.createRefreshToken(refreshClaims);

        // 6c. Hash and persist the new refresh token
        String newTokenHash = TokenHashUtil.hash(newRefreshToken);
        Instant expiredAt = Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
        RefreshToken newEntity = RefreshToken.create(claims.userId(), newTokenHash, expiredAt);
        refreshTokenWriteRepository.save(newEntity);

        // 7. Return the new tokens
        return new RefreshTokenCommandResultDto(newAccessToken, newRefreshToken);
    }
}
