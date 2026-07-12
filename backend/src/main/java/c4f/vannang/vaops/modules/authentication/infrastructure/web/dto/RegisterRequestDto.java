package c4f.vannang.vaops.modules.authentication.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(


    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 256, message = "Account name must be between 1 and 256 characters")
    String accountName,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 256, message = "Password must be between 8 and 256 characters")
    String password,

    @NotBlank(message = "Display name is required")
    @Size(min = 1, max = 256, message = "Display name must be between 1 and 256 characters")
    String displayName,

    String avatarUrl

) {}
