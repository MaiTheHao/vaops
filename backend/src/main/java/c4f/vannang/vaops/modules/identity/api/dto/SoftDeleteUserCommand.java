package c4f.vannang.vaops.modules.identity.api.dto;

import java.util.UUID;

public record SoftDeleteUserCommand(
    UUID userId,
    UUID deletedBy
) {}
