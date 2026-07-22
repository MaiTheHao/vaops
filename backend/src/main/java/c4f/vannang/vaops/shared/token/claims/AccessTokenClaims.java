package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record AccessTokenClaims(UUID userId, String accountName) implements TokenClaims {
}
