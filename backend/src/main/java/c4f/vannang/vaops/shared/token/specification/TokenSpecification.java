package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.TokenClaims;

public interface TokenSpecification<C extends TokenClaims> {
    String generate(C claims);
    C validate(String token);
}
