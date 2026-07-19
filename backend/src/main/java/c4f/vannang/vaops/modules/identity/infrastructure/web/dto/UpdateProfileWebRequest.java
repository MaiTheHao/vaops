package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileWebRequest(
    @NotBlank(message = "Display name is required")
    String displayName,
    String avatarUrl
) {}
