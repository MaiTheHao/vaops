package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record RefreshTokenClaims(UUID userId) implements TokenClaims {
}
