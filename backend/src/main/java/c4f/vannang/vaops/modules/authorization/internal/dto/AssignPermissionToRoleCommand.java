package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.Set;
import java.util.UUID;

public record AssignPermissionToRoleCommand(
    UUID roleId,
    Set<UUID> permissionIds,
    UUID updatedBy
) {}
