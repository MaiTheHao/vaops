package c4f.vannang.vaops.modules.authentication.api.dto;

public record LoginCommandResultDto(

		String accessToken,

		String refreshToken

) {
}
