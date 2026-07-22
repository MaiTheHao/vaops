package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;

public interface RefreshTokenSpec {
    String generate(RefreshTokenClaims claims);
    RefreshTokenClaims validateRefreshToken(String token);
}
