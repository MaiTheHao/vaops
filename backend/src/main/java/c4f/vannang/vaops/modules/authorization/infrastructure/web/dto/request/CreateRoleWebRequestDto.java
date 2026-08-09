package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new role")
public record CreateRoleWebRequestDto(
        @Schema(description = "Unique role code", example = "ADMIN")
        @NotBlank(message = "code is required")
        @Size(max = 256, message = "code must not exceed 256 characters")
        String code,

        @Schema(description = "Role description", example = "Administrator role with elevated permissions")
        @Size(max = 1024, message = "description must not exceed 1024 characters")
        String description
) {}