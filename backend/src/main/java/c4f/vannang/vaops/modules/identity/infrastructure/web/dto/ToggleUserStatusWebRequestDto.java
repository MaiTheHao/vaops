package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public record ToggleUserStatusWebRequestDto(@NotNull Boolean active) {}
