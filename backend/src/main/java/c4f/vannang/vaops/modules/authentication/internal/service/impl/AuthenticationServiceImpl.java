package c4f.vannang.vaops.modules.authentication.internal.service.impl;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenQueryRepository;
import c4f.vannang.vaops.modules.authentication.internal.repository.RefreshTokenWriteRepository;
import c4f.vannang.vaops.modules.authentication.internal.service.AuthenticationService;
import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import c4f.vannang.vaops.modules.authorization.api.service.AuthorizationAPIService;
import c4f.vannang.vaops.modules.authorization.api.util.PermissionUtils;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.exception.AbstractPlatformException;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategy;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.RefreshTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.feature.token.claims.RefreshTokenClaims;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class AuthenticationServiceImpl implements AuthenticationService {

  private final PasswordEncoder passwordEncoder;
  private final IdentityUserAPIService identityUserService;
  private final AccessTokenSpec accessTokenSpec;
  private final RefreshTokenSpec refreshTokenSpec;
  private final AuthProperties authProperties;
  private final RefreshTokenQueryRepository refreshTokenQueryRepository;
  private final RefreshTokenWriteRepository refreshTokenWriteRepository;
  private final DeterministicHashStrategyFactory deterministicHashStrategyFactory;
  private final AuthorizationAPIService authorizationAPIService;

  private static final String DUMMY_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi8Lh5W4YN8V8k9w9V3d5zP2Vx1bG0y";

  @Override
  public LoginCommandResult login(LoginCommand command) {
    try {
      UserAuthDto userAuth = identityUserService
          .getUserForAuth(new FindForAuthQuery(command.accountName()))
          .orElse(null);

      if (userAuth == null) {
        passwordEncoder.matches(command.password(), DUMMY_HASH);
        throw new UnauthenticatedException("Invalid credentials");
      }

      boolean passwordMatches = passwordEncoder.matches(command.password(), userAuth.passwordHash());

      if (userAuth.lockedUntil() != null && Instant.now().isBefore(userAuth.lockedUntil())) {
        throw new UnauthenticatedException("Invalid credentials");
      }

      if (!userAuth.active()) {
        throw new UnauthenticatedException("Invalid credentials");
      }

      if (!passwordMatches) {
        try {
          identityUserService.recordFailedLogin(new RecordFailedLoginRequest(command.accountName()));
        } catch (RuntimeException e) {
          log.warn("Failed to record failed login", e);
        }
        throw new UnauthenticatedException("Invalid credentials");
      }

      UUID userId = userAuth.id();

      AccessTokenClaims accessClaims = buildAccessTokenClaims(userId, command.accountName());
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

      identityUserService.recordSuccessfulLogin(new RecordSuccessfulLoginRequest(userId));

      return new LoginCommandResult(accessToken, refreshToken);

    } catch (AbstractPlatformException e) {
      throw e;
    } catch (Exception e) {
      log.error("Login failed: {}", e.getMessage(), e);
      throw new InternalServerException("Unexpected error while logging in. Please try again.", e);
    }
  }

  @Override
  public RegisterCommandResult register(RegisterCommand command) {
    try {
      RegisterRequest identityRegisterRequest = new RegisterRequest(command.accountName(), command.password(), command.displayName(),
              command.avatarUrl());

      UserDto registeredUser = identityUserService.register(identityRegisterRequest);

      return new RegisterCommandResult(registeredUser.id(), registeredUser.accountName(),
              registeredUser.displayName(), registeredUser.avatarUrl());
    } catch (AbstractPlatformException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during registration for account: {}", command.accountName(), e);
      throw new InternalServerException("Unexpected error while registering. Please try again.", e);
    }
  }

  @Override
  @Transactional
  public RefreshTokenCommandResult refreshToken(RefreshTokenCommand command) {
    RefreshTokenClaims claims = refreshTokenSpec.validate(command.refreshToken());
    DeterministicHashStrategy hashStrategy =
        deterministicHashStrategyFactory.getStrategy(DeterministicHashAlgorithm.SHA_256);

    String tokenHash = hashStrategy.hash(command.refreshToken());
    RefreshToken storedToken = refreshTokenQueryRepository
        .findByTokenHash(tokenHash)
        .orElseThrow(() -> new UnauthenticatedException("Invalid refresh token"));

    if (storedToken.isExpired()) throw new UnauthenticatedException("Refresh token is expired");
    if (storedToken.isRevoked()) {
      Instant revokedAt = storedToken.getRevokedAt();
      boolean withinGraceWindow =
          revokedAt != null && Instant.now().isBefore(revokedAt.plusSeconds(authProperties.getRefreshGraceWindowSeconds()));

      if (!withinGraceWindow) {
        List<RefreshToken> activeTokens =
            refreshTokenQueryRepository.findValidRefreshTokensByUserId(claims.userId());
        activeTokens.forEach(refreshToken -> refreshToken.revoke());
        refreshTokenWriteRepository.saveAll(activeTokens);

        throw new UnauthenticatedException(
            "Refresh token has been revoked previously. Potential breach detected.");
      }

      throw new UnauthenticatedException(
          "Refresh token has already been used within grace window.");
    }

    UserDto user = identityUserService
        .getUserById(new FindByIdQuery(claims.userId()))
        .orElseThrow(() -> new UnauthenticatedException("User not found"));

    if (!user.active()) throw new UnauthenticatedException("User account is inactive");

    List<RefreshToken> tokensToSave = new ArrayList<>();
    storedToken.revoke();
    tokensToSave.add(storedToken);

    AccessTokenClaims accessClaims = buildAccessTokenClaims(claims.userId(), user.accountName());
    RefreshTokenClaims refreshClaims = new RefreshTokenClaims(claims.userId());
    String newAccessToken = accessTokenSpec.generate(accessClaims);
    String newRefreshToken = refreshTokenSpec.generate(refreshClaims);
    String newTokenHash = hashStrategy.hash(newRefreshToken);

    Instant expiredAt = Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
    RefreshToken newEntity = RefreshToken.create(claims.userId(), newTokenHash, expiredAt);
    tokensToSave.add(newEntity);
    refreshTokenWriteRepository.saveAll(tokensToSave);

    return new RefreshTokenCommandResult(newAccessToken, newRefreshToken);
  }

  @Override
  @Transactional
  public LogoutCommandResult logout(LogoutCommand command) {
    String tokenHash = deterministicHashStrategyFactory
        .getStrategy(DeterministicHashAlgorithm.SHA_256)
        .hash(command.refreshToken());

    Optional<RefreshToken> storedToken = refreshTokenQueryRepository.findByTokenHash(tokenHash);

    if (storedToken.isPresent()) {
      storedToken.get().revoke();
      refreshTokenWriteRepository.save(storedToken.get());
      return new LogoutCommandResult(true);
    }

    return new LogoutCommandResult(false);
  }

  private AccessTokenClaims buildAccessTokenClaims(UUID userId, String accountName) {
    List<RoleDto> roleDtos = authorizationAPIService.getRolesByUserId(userId);
    List<PermissionDto> permDtos = authorizationAPIService.getPermissionsByUserId(userId);

    List<String> roles = roleDtos.stream().map(RoleDto::code).toList();
    List<String> permissions = permDtos.stream().map(PermissionUtils::format).toList();

    return new AccessTokenClaims(userId, accountName, roles, permissions);
  }
}
