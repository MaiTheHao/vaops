package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

public record AssignRolesToUserWebRequestDto(
        @NotEmpty(message = "roleIds must not be empty")
        Set<UUID> roleIds
) {}