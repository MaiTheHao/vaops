package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record EmailTokenClaims(UUID userId, String email) implements TokenClaims {
}
