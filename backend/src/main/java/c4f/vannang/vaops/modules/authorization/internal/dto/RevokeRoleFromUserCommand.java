package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.UUID;

public record RevokeRoleFromUserCommand(
    UUID userId,
    UUID roleId,
    UUID revokedBy
) {}
