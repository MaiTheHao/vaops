package c4f.vannang.vaops.modules.identity.api.dto;

import java.time.Instant;
import java.util.UUID;

public record UserDto(

		UUID id,

		String accountName,

		String displayName,

		String avatarUrl,

		boolean active,

		Instant lastLoginAt,

		Instant createdAt,

		Instant updatedAt

) {
}
