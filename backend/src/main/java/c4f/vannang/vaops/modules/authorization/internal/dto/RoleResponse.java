package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String code,
    String description,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    Set<PermissionResponse> permissions
) {}
