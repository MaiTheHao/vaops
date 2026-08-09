package c4f.vannang.vaops.modules.authentication.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User login credentials payload")
public record LoginWebRequestDto(

    @Schema(description = "Account username or email", example = "john_doe")
    @NotBlank(message = "Account name is required")
    String accountName,

    @Schema(description = "User account password", example = "P@ssw0rd123")
    @NotBlank(message = "Password is required")
    String password

) {}

