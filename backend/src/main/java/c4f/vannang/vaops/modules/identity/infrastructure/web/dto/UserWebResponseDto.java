package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "User account response payload")
public record UserWebResponseDto(
    @Schema(description = "Unique user ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Account username", example = "john_doe")
    String accountName,

    @Schema(description = "User display name", example = "John Doe")
    String displayName,

    @Schema(description = "Avatar image URL", example = "https://example.com/avatar.jpg")
    String avatarUrl,

    @Schema(description = "Whether the user account is active", example = "true")
    boolean active,

    @Schema(description = "Timestamp of last login", example = "2026-08-09T10:00:00Z")
    Instant lastLoginAt,

    @Schema(description = "Account creation timestamp", example = "2026-01-01T00:00:00Z")
    Instant createdAt,

    @Schema(description = "Account last updated timestamp", example = "2026-08-09T12:00:00Z")
    Instant updatedAt
) {}

