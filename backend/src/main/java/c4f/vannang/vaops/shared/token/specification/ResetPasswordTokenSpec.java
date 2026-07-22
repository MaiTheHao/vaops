package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.ResetPasswordTokenClaims;

public interface ResetPasswordTokenSpec {
    String generate(ResetPasswordTokenClaims claims);
    ResetPasswordTokenClaims validateResetPasswordToken(String token);
}
