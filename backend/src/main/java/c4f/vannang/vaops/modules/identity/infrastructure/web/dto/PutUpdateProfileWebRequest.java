package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PutUpdateProfileWebRequest(
    
    @NotBlank(message = "Display name is required") String displayName,

    @NotBlank(message = "Avatar url is required") String avatarUrl) {}

