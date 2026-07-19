package c4f.vannang.vaops.modules.authentication.api.dto;

public record RegisterRequestDto(

		String accountName,

		String password,

		String displayName,

		String avatarUrl

) {
}
