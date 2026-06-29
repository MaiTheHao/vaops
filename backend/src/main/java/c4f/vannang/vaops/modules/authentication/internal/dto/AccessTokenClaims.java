package c4f.vannang.vaops.modules.authentication.internal.dto;

import java.util.Map;
import java.util.UUID;

public record AccessTokenClaims(UUID userId, String accountName) {
    public Map<String, Object> toClaimsMap() {
        return Map.of("sub", accountName, "userId", userId.toString());
    }
}
