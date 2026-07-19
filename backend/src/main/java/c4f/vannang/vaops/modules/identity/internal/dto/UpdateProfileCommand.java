package c4f.vannang.vaops.modules.identity.internal.dto;

import java.util.UUID;

public record UpdateProfileCommand(UUID userId, String displayName, String avatarUrl) {}
