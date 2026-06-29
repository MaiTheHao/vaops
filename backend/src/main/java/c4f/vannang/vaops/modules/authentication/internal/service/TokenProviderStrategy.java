package c4f.vannang.vaops.modules.authentication.internal.service;

import c4f.vannang.vaops.modules.authentication.internal.dto.AccessTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;

public interface TokenProviderStrategy {
    TokenType getType();

    String createAccessToken(AccessTokenClaims claims);

    String createRefreshToken(RefreshTokenClaims claims);

    AccessTokenClaims validateAccessToken(String token);

    RefreshTokenClaims validateRefreshToken(String token);
}
