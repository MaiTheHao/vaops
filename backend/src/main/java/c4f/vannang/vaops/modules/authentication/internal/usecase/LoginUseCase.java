package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.exception.AccountLockedException;
import c4f.vannang.vaops.modules.authentication.api.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.dto.AccessTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderFactory;
import c4f.vannang.vaops.modules.authentication.internal.service.TokenProviderStrategy;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.authentication.internal.util.TokenHashUtil;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final PasswordEncoder passwordEncoder;
    private final IdentityModuleApi identityModuleApi;
    private final TokenProviderFactory tokenServiceFactory;
    private final AuthProperties authProperties;
    private final RefreshTokenWriteRepository refreshTokenWriteRepository;

    public LoginCommandResultDto execute(LoginCommandDto dto) {
        try {
            UserAuthDto userAuth = identityModuleApi.getUserForAuth(new FindForAuthQuery(dto.accountName()))
                    .orElseThrow(() -> new UnauthenticatedException("Invalid credentials"));

            if (userAuth.lockedUntil() != null && Instant.now().isBefore(userAuth.lockedUntil())) {
                throw new AccountLockedException("Account is locked until " + userAuth.lockedUntil());
            }

            if (!userAuth.active()) {
                throw new UnauthenticatedException("Account is deactivated");
            }

            if (!passwordEncoder.matches(dto.password(), userAuth.passwordHash())) {
                identityModuleApi.recordFailedLogin(new RecordFailedLoginCommand(dto.accountName()));
                throw new UnauthenticatedException("Invalid credentials");
            }

            UUID userId = userAuth.id();

            AccessTokenClaims accessClaims = new AccessTokenClaims(userId, dto.accountName());
            RefreshTokenClaims refreshClaims = new RefreshTokenClaims(userId);

            TokenProviderStrategy tokenService = tokenServiceFactory.getService(TokenType.JWT);
            String accessToken = tokenService.createAccessToken(accessClaims);
            String refreshToken = tokenService.createRefreshToken(refreshClaims);

            // Persist refresh token hash for rotation
            String tokenHash = TokenHashUtil.hash(refreshToken);
            Instant expiredAt = Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
            RefreshToken entity = RefreshToken.create(userId, tokenHash, expiredAt);
            refreshTokenWriteRepository.save(entity);

            identityModuleApi.recordSuccessfulLogin(new RecordSuccessfulLoginCommand(userId));

            return new LoginCommandResultDto(accessToken, refreshToken);

        } catch (UnauthenticatedException | AccountLockedException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while logging in. Please try again.", e);
        }
    }
}
