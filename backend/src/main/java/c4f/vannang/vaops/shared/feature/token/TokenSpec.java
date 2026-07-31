package c4f.vannang.vaops.shared.feature.token;

import c4f.vannang.vaops.shared.feature.token.claims.TokenClaims;

public interface TokenSpec<C extends TokenClaims> {

  String generate(C claims);

  C validate(String token);
}
