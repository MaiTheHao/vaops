package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;

public interface AccessTokenSpec {
    String generate(AccessTokenClaims claims);
    AccessTokenClaims validateAccessToken(String token);
}
