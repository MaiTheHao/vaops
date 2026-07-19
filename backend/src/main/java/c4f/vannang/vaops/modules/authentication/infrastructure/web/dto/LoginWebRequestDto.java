package c4f.vannang.vaops.modules.authentication.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginWebRequestDto(

    @NotBlank(message = "Account name is required") String accountName,

    @NotBlank(message = "Password is required") String password

) {}
