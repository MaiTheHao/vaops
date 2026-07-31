package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.UUID;

public record UpdateRoleCommand(
    UUID id,
    String code,
    String description
) {}
