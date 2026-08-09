package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating an existing role")
public record UpdateRoleWebRequestDto(
        @Schema(description = "Unique role code", example = "MANAGER")
        @NotBlank(message = "code is required")
        @Size(max = 256, message = "code must not exceed 256 characters")
        String code,

        @Schema(description = "Role description", example = "Managerial role with operational permissions")
        @Size(max = 1024, message = "description must not exceed 1024 characters")
        String description
) {}