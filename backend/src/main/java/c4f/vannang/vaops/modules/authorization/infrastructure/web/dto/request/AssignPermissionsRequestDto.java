package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Request body for assigning permissions to a role")
public record AssignPermissionsRequestDto(
        @Schema(description = "Set of permission IDs to assign to the role")
        @NotEmpty(message = "permissionIds must not be empty")
        Set<UUID> permissionIds
) {}