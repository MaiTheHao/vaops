package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for enabling or disabling user account status")
public record ToggleUserStatusWebRequestDto(
    @Schema(description = "Target active status", example = "true")
    @NotNull Boolean active
) {}


