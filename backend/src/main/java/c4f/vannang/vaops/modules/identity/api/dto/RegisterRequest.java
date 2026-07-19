package c4f.vannang.vaops.modules.identity.api.dto;

public record RegisterRequest(
    String accountName,
    String rawPassword,
    String displayName,
    String avatarUrl
) {}
