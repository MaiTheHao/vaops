package c4f.vannang.vaops.modules.authorization.internal.dto;

public record CreatePermissionCommand(
    String resource,
    String action,
    String description
) {}
