package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleWebRequestDto(
        @NotBlank(message = "code is required")
        @Size(max = 256, message = "code must not exceed 256 characters")
        String code,

        @Size(max = 1024, message = "description must not exceed 1024 characters")
        String description
) {}