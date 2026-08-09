package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Request body for revoking roles from a user")
public record RevokeRoleFromUserWebRequestDto(
        @Schema(description = "Set of role IDs to revoke from the user")
        @NotEmpty(message = "roleIds must not be empty")
        Set<UUID> roleIds
) {}