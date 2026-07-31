package c4f.vannang.vaops.shared.feature.token.claims;

import java.util.UUID;

public record AccessTokenClaims(UUID userId, String accountName) implements TokenClaims {
}
