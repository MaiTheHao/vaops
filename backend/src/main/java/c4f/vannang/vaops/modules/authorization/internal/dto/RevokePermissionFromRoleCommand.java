package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.UUID;

public record RevokePermissionFromRoleCommand(
    UUID roleId,
    UUID permissionId,
    UUID updatedBy
) {}
