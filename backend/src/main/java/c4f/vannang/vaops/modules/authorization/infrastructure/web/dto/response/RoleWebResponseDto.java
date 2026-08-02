package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RoleWebResponseDto(
        UUID id,
        String code,
        String description,
        boolean active,
        Set<PermissionWebResponseDto> permissions,
        Instant createdAt,
        Instant updatedAt
) {}