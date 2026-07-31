package c4f.vannang.vaops.shared.feature.token.claims;

import java.util.UUID;

public record EmailTokenClaims(UUID userId, String email) implements TokenClaims {
}
