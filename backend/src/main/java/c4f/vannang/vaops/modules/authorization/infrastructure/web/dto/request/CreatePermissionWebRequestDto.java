package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a new permission")
public record CreatePermissionWebRequestDto(
        @Schema(description = "Target domain resource", example = "USER")
        @NotBlank(message = "resource is required")
        @Size(max = 256, message = "resource must not exceed 256 characters")
        String resource,

        @Schema(description = "Permission action name", example = "READ")
        @NotBlank(message = "action is required")
        @Size(max = 256, message = "action must not exceed 256 characters")
        String action,

        @Schema(description = "Detailed permission description", example = "Allows reading user profiles")
        @Size(max = 1024, message = "description must not exceed 1024 characters")
        String description
) {}