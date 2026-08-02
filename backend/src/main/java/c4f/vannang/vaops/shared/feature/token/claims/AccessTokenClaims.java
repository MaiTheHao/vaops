package c4f.vannang.vaops.shared.feature.token.claims;

import java.util.List;
import java.util.UUID;

public record AccessTokenClaims(
    UUID userId,
    String accountName,
    List<String> roles,
    List<String> permissions
) implements TokenClaims {

  public AccessTokenClaims {
    roles = roles == null ? List.of() : roles;
    permissions = permissions == null ? List.of() : permissions;
  }

  public AccessTokenClaims(UUID userId, String accountName) {
    this(userId, accountName, List.of(), List.of());
  }
}