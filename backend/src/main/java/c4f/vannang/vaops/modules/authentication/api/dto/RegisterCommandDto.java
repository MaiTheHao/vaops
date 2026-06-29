package c4f.vannang.vaops.modules.authentication.api.dto;

public record RegisterCommandDto(

		String accountName,

		String password,

		String displayName,

		String avatarUrl

) {
}
