package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Role details response payload with associated permissions")
public record RoleWebResponseDto(
        @Schema(description = "Unique role ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Role code identifier", example = "ADMIN")
        String code,

        @Schema(description = "Role description", example = "Administrator with full system access")
        String description,

        @Schema(description = "Whether the role is active", example = "true")
        boolean active,

        @Schema(description = "Set of permissions assigned to this role")
        Set<PermissionWebResponseDto> permissions,

        @Schema(description = "Role creation timestamp", example = "2026-01-01T00:00:00Z")
        Instant createdAt,

        @Schema(description = "Role last update timestamp", example = "2026-08-09T12:00:00Z")
        Instant updatedAt
) {}