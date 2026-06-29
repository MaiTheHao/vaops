package c4f.vannang.vaops.modules.identity.api.dto;

import java.time.Instant;
import java.util.UUID;

public record UserAuthDto(

        UUID id,

        String passwordHash,

        Instant lockedUntil,

        boolean active

) {
}
