package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionWebRequestDto(
        @NotBlank(message = "resource is required")
        @Size(max = 256, message = "resource must not exceed 256 characters")
        String resource,

        @NotBlank(message = "action is required")
        @Size(max = 256, message = "action must not exceed 256 characters")
        String action,

        @Size(max = 1024, message = "description must not exceed 1024 characters")
        String description
) {}