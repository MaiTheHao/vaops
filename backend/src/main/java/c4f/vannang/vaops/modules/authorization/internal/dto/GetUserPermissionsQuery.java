package c4f.vannang.vaops.modules.authorization.internal.dto;

import java.util.UUID;

public record GetUserPermissionsQuery(
    UUID userId
) {}
