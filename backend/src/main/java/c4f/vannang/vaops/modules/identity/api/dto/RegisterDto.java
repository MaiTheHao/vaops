package c4f.vannang.vaops.modules.identity.api.dto;

public record RegisterDto(
    String accountName,
    String rawPassword,
    String displayName,
    String avatarUrl
) {}
