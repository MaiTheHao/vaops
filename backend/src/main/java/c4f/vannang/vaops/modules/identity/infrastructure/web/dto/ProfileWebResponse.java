package c4f.vannang.vaops.modules.identity.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ProfileWebResponse(
    UUID id,
    String accountName,
    String displayName,
    String avatarUrl,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt
) {}
