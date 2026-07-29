package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record PermissionResponse(
    UUID id,
    String resource,
    String action,
    String description,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt,
    Instant deletedAt,
    UUID deletedBy,
    UUID createdBy,
    UUID updatedBy
) {}
