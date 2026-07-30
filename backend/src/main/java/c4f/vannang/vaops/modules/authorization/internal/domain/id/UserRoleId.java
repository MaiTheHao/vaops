package c4f.vannang.vaops.modules.authorization.internal.domain.id;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Composite primary key for user_roles table
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserRoleId implements Serializable {

  @Column(name = "user_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID userId;

  @Column(name = "role_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID roleId;

  public UserRoleId(UUID userId, UUID roleId) {
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
    this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
  }
}
