package c4f.vannang.vaops.modules.authentication.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request payload")
public record RegisterWebRequestDto(

    @Schema(description = "Account username", example = "john_doe")
    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 256, message = "Account name must be between 1 and 256 characters")
    String accountName,

    @Schema(description = "Account password", example = "P@ssw0rd123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 256, message = "Password must be between 8 and 256 characters")
    String password,

    @Schema(description = "User display name", example = "John Doe")
    @NotBlank(message = "Display name is required")
    @Size(min = 1, max = 256, message = "Display name must be between 1 and 256 characters")
    String displayName,

    @Schema(description = "Avatar image URL", example = "https://example.com/avatar.jpg")
    String avatarUrl

) {}


