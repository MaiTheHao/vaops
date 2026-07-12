package c4f.vannang.vaops.modules.identity.api.dto;

import java.util.UUID;

public record ToggleUserStatusCommand(
    UUID userId,
    boolean active
) {}
