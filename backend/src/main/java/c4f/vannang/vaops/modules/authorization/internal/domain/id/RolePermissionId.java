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

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RolePermissionId implements Serializable {
  
  @Column(name = "role_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID roleId;

  @Column(name = "permission_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID permissionId;

  public RolePermissionId(UUID roleId, UUID permissionId) {
    this.roleId = Objects.requireNonNull(roleId, "roleId must not be null");
    this.permissionId = Objects.requireNonNull(permissionId, "permissionId must not be null");
  }
}
