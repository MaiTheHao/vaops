package c4f.vannang.vaops.shared.feature.security;

import java.util.UUID;

public record AuthenticatedPrincipal(UUID userId, String accountName) {}
