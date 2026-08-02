package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PermissionWebResponseDto(
        UUID id,
        String code,
        String resource,
        String action,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}