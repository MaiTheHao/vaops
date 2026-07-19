package c4f.vannang.vaops.modules.identity.api.dto;

import java.util.UUID;

public record UpdateProfileRequest(
    UUID userId,
    String displayName,
    String avatarUrl
) {}
