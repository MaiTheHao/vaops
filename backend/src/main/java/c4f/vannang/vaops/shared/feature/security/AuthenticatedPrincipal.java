package c4f.vannang.vaops.shared.feature.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedPrincipal(
    UUID userId,
    String accountName,
    List<String> roles,
    List<String> permissions
) {
  public AuthenticatedPrincipal {
    roles = roles == null ? List.of() : roles;
    permissions = permissions == null ? List.of() : permissions;
  }

  public AuthenticatedPrincipal(UUID userId, String accountName) {
    this(userId, accountName, List.of(), List.of());
  }
}

