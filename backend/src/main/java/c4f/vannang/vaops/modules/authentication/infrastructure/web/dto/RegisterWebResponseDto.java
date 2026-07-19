package c4f.vannang.vaops.modules.authentication.infrastructure.web.dto;

import java.util.UUID;

public record RegisterWebResponseDto(

    UUID id,

    String accountName,

    String displayName,

    String avatarUrl

) {}
