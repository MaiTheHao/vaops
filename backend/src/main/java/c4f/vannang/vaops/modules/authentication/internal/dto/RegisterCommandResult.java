package c4f.vannang.vaops.modules.authentication.internal.dto;

import java.util.UUID;

public record RegisterCommandResult(
    UUID id,
    String accountName,
    String displayName,
    String avatarUrl
) {}
