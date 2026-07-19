package c4f.vannang.vaops.modules.authentication.internal.dto;

public record RegisterCommand(
    String accountName,
    String password,
    String displayName,
    String avatarUrl
) {}
