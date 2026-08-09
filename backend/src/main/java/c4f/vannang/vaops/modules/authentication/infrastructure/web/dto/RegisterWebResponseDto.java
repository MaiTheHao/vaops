package c4f.vannang.vaops.modules.authentication.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "User registration response payload")
public record RegisterWebResponseDto(

    @Schema(description = "Newly registered user ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Account username", example = "john_doe")
    String accountName,

    @Schema(description = "User display name", example = "John Doe")
    String displayName,

    @Schema(description = "Avatar image URL", example = "https://example.com/avatar.jpg")
    String avatarUrl

) {}

