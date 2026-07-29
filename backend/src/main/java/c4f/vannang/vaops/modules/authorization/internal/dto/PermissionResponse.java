package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
    UUID id,
    String resource,
    String action,
    String description,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {}
