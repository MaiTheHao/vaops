package c4f.vannang.vaops.module.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PartialUpdateUserDto(
    @Email(message = "Email should be valid")
    @Size(max = 256)
    String email,

    @Size(max = 30)
    String phone,

    @Size(max = 256)
    String fullName,

    @Size(max = 256)
    String givenName,

    @Size(max = 256)
    String familyName,

    @Size(max = 1024)
    String avatarUrl
) {}
