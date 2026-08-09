package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for changing user password")
public record ChangePasswordWebRequest(
    @Schema(description = "Current user password", example = "OldP@ssw0rd123")
    @NotBlank(message = "Old password is required")
    String oldPassword,

    @Schema(description = "New user password", example = "NewP@ssw0rd123")
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String newPassword
) {}
