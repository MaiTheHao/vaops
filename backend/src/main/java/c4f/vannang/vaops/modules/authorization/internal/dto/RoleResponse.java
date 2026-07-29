package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;

@Builder
public record RoleResponse(
    UUID id,
    String code,
    String description,
    Boolean isActive,
    Set<PermissionResponse> permissions,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt,
    UUID deletedBy,
    UUID createdBy,
    UUID updatedBy
) {}
