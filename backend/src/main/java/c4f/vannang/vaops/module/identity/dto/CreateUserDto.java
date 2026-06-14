package c4f.vannang.vaops.module.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateUserDto(
    @Email(message = "Email should be valid")
    @Size(max = 256)
    String email,

    @Size(max = 30)
    String phone,

    @NotBlank(message = "Full name is required")
    @Size(max = 256)
    String fullName,

    @Size(max = 256)
    String givenName,

    @Size(max = 256)
    String familyName,

    @Size(max = 1024)
    String avatarUrl,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password
) {}
