package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.Set;
import java.util.UUID;

public record CreateRoleCommand(
    String code,
    String description,
    Set<UUID> permissionIds
) {}
