package c4f.vannang.vaops.modules.authentication.internal.dto;

import java.util.Map;
import java.util.UUID;

public record RefreshTokenClaims(UUID userId) {
    public Map<String, Object> toClaimsMap() {
        return Map.of("sub", userId.toString());
    }
}
