package c4f.vannang.vaops.modules.authentication.internal.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

    @NotBlank(message = "Account name is required") String accountName,

    @NotBlank(message = "Password is required") String password

) {
}
