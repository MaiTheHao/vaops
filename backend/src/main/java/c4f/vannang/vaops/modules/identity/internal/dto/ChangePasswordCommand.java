package c4f.vannang.vaops.modules.identity.internal.dto;

import java.util.UUID;

public record ChangePasswordCommand(UUID userId, String oldPassword, String newPassword) {}
