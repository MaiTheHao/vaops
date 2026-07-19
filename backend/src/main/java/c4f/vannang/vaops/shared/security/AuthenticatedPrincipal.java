package c4f.vannang.vaops.shared.security;

import java.util.UUID;

/**
 * Immutable principal representing a successfully authenticated user.
 * Token-mechanism-agnostic — built from verified token claims after
 * account state is confirmed via UserDetailsService.
 */
public record AuthenticatedPrincipal(UUID userId, String accountName) {}
