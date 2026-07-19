package c4f.vannang.vaops.modules.identity.internal.dto;

public record RegisterCommand(
    String accountName,
    String rawPassword,
    String displayName,
    String avatarUrl
) {}
