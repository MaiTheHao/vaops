package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Request body for revoking permissions from a role")
public record RevokePermissionsRequestDto(
        @Schema(description = "Set of permission IDs to revoke from the role")
        @NotEmpty(message = "permissionIds must not be empty")
        Set<UUID> permissionIds
) {}