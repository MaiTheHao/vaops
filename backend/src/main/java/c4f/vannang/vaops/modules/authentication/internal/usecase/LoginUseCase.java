package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.service.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import c4f.vannang.vaops.shared.token.specification.RefreshTokenSpec;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

  private final PasswordEncoder passwordEncoder;
  private final IdentityModuleApi identityModuleApi;
  private final AccessTokenSpec accessTokenSpec;
  private final RefreshTokenSpec refreshTokenSpec;
  private final AuthProperties authProperties;
  private final RefreshTokenWriteRepository refreshTokenWriteRepository;
  private final DeterministicHashStrategyFactory deterministicHashStrategyFactory;

  public LoginCommandResult execute(LoginCommand command) {
    try {
      UserAuthDto userAuth = identityModuleApi
          .getUserForAuth(new FindForAuthQuery(command.accountName()))
          .orElseThrow(() -> new UnauthenticatedException("Invalid credentials"));

      if (userAuth.lockedUntil() != null && Instant.now().isBefore(userAuth.lockedUntil())) {
        throw new AccountLockedException("Account is locked until " + userAuth.lockedUntil());
      }

      if (!userAuth.active()) {
        throw new UnauthenticatedException("Account is deactivated");
      }

      if (!passwordEncoder.matches(command.password(), userAuth.passwordHash())) {
        identityModuleApi.recordFailedLogin(new RecordFailedLoginRequest(command.accountName()));
        throw new UnauthenticatedException("Invalid credentials");
      }

      UUID userId = userAuth.id();

      AccessTokenClaims accessClaims = new AccessTokenClaims(userId, command.accountName());
      RefreshTokenClaims refreshClaims = new RefreshTokenClaims(userId);

      String accessToken = accessTokenSpec.generate(accessClaims);
      String refreshToken = refreshTokenSpec.generate(refreshClaims);

      String tokenHash = deterministicHashStrategyFactory
          .getStrategy(DeterministicHashAlgorithm.SHA_256)
          .hash(refreshToken);

      Instant expiredAt =
          Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
      RefreshToken entity = RefreshToken.create(userId, tokenHash, expiredAt);
      refreshTokenWriteRepository.save(entity);

      identityModuleApi.recordSuccessfulLogin(new RecordSuccessfulLoginRequest(userId));

      return new LoginCommandResult(accessToken, refreshToken);

    } catch (UnauthenticatedException | AccountLockedException e) {
      throw e;
    } catch (Exception e) {
      throw new InternalServerException("Unexpected error while logging in. Please try again.", e);
    }
  }
}
