package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.EmailTokenClaims;

public interface EmailTokenSpec {
    String generate(EmailTokenClaims claims);
    EmailTokenClaims validateEmailToken(String token);
}
