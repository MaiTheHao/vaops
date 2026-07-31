package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.Set;
import java.util.UUID;

public record AssignRolesToUserCommand(
    UUID userId,
    Set<UUID> roleIds,
    UUID assignedBy
) {}
