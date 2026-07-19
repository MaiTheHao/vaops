package c4f.vannang.vaops.modules.identity.api.dto;

import java.util.UUID;

public record SoftDeleteUserRequest(
    UUID userId,
    UUID deletedBy
) {}
