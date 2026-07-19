package c4f.vannang.vaops.modules.identity.internal.dto;

import java.util.UUID;

public record ToggleUserStatusCommand(UUID userId, boolean active) {}
