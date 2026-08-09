package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for updating user profile information")
public record PutUpdateProfileWebRequest(
    
    @Schema(description = "User display name", example = "John Doe")
    @NotBlank(message = "Display name is required")
    String displayName,

    @Schema(description = "User avatar image URL", example = "https://example.com/avatar.jpg")
    @NotBlank(message = "Avatar url is required")
    String avatarUrl) {}


