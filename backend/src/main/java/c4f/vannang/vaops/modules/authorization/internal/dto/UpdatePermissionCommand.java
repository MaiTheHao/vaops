package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.UUID;

public record UpdatePermissionCommand(
    UUID id,
    String resource,
    String action,
    String description
) {}
