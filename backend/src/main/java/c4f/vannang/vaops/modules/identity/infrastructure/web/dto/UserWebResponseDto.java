package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserWebResponseDto(
    UUID id,
    String accountName,
    String displayName,
    String avatarUrl,
    boolean active,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt) {}
