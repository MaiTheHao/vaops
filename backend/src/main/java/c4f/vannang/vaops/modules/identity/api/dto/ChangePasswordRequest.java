package c4f.vannang.vaops.modules.identity.api.dto;

import java.util.UUID;

public record ChangePasswordRequest(
    UUID userId,
    String oldPassword,
    String newPassword
) {}
