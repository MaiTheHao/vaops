package c4f.vannang.vaops.modules.identity.api.dto;

import java.util.UUID;

public record UpdateProfileCommand(
    UUID userId,
    String displayName,
    String avatarUrl
) {}
