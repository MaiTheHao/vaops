package c4f.vannang.vaops.module.identity.dto;

import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Builder
public record SafeUserDto(
    UUID id,
    String email,
    String phone,
    String fullName,
    String givenName,
    String familyName,
    String avatarUrl,
    boolean isActive,
    Instant lastLoginAt,
    Instant createdAt,
    Instant updatedAt
) {}
