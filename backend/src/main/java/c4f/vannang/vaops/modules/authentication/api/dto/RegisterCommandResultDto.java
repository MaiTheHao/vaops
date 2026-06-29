package c4f.vannang.vaops.modules.authentication.api.dto;

import java.util.UUID;

public record RegisterCommandResultDto(

		UUID id,

		String accountName,

		String displayName,

		String avatarUrl

) {
}
