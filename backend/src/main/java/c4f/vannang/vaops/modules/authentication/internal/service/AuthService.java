package c4f.vannang.vaops.modules.authentication.internal.service;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.exception.AccountLockedException;
import c4f.vannang.vaops.modules.authentication.api.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.dto.AccessTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final IdentityModuleApi identityModuleApi;
    private final TokenProviderFactory tokenServiceFactory;

    public LoginCommandResultDto login(LoginCommandDto dto) {
        try {
            UserAuthDto userAuth = identityModuleApi.getUserForAuth(dto.accountName())
                    .orElseThrow(() -> new UnauthenticatedException("Invalid credentials"));

            if (userAuth.lockedUntil() != null && Instant.now().isBefore(userAuth.lockedUntil())) {
                throw new AccountLockedException("Account is locked until " + userAuth.lockedUntil());
            }

            if (!userAuth.active()) {
                throw new UnauthenticatedException("Account is deactivated");
            }

            if (!passwordEncoder.matches(dto.password(), userAuth.passwordHash())) {
                identityModuleApi.recordFailedLogin(dto.accountName());
                throw new UnauthenticatedException("Invalid credentials");
            }

            UUID userId = userAuth.id();

            AccessTokenClaims accessClaims = new AccessTokenClaims(userId, dto.accountName());
            RefreshTokenClaims refreshClaims = new RefreshTokenClaims(userId);

            TokenProviderStrategy tokenService = tokenServiceFactory.getService(TokenType.JWT);
            String accessToken = tokenService.createAccessToken(accessClaims);
            String refreshToken = tokenService.createRefreshToken(refreshClaims);

            identityModuleApi.recordSuccessfulLogin(userId);

            return new LoginCommandResultDto(accessToken, refreshToken);

        } catch (UnauthenticatedException | AccountLockedException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while logging in. Please try again.");
        }
    }

    public RegisterCommandResultDto register(RegisterCommandDto dto) {
        try {
            RegisterDto identityRegisterDto = new RegisterDto(dto.accountName(), dto.password(), dto.displayName(),
                    dto.avatarUrl());

            UserDto registeredUser = identityModuleApi.register(identityRegisterDto);

            return new RegisterCommandResultDto(registeredUser.id(), registeredUser.accountName(),
                    registeredUser.displayName(), registeredUser.avatarUrl());
        } catch (ValidationException | ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while registering. Please try again.");
        }
    }
}
