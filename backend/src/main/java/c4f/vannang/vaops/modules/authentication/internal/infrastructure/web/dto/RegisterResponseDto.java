package c4f.vannang.vaops.modules.authentication.internal.infrastructure.web.dto;

import java.util.UUID;

public record RegisterResponseDto(

    UUID id,

    String accountName,

    String displayName,

    String avatarUrl

) {
}
