package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.Set;
import java.util.UUID;

public record AssignPermissionsToRoleCommand(
    UUID roleId,
    Set<UUID> permissionIds
) {}
