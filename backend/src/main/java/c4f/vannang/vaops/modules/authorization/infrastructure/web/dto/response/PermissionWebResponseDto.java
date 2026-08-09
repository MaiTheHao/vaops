package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Permission details response payload")
public record PermissionWebResponseDto(
        @Schema(description = "Unique permission ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Permission code (resource:action format)", example = "USER:READ")
        String code,

        @Schema(description = "Target resource", example = "USER")
        String resource,

        @Schema(description = "Permission action", example = "READ")
        String action,

        @Schema(description = "Permission description", example = "Allows reading user profiles")
        String description,

        @Schema(description = "Whether the permission is active", example = "true")
        boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-01T00:00:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-08-09T12:00:00Z")
        Instant updatedAt
) {}