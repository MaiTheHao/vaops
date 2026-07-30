package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.RolePermissionId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission {
  
  @EmbeddedId
  private RolePermissionId id;

  public static RolePermission assign(UUID roleId, UUID permissionId) {
    RolePermission rp = new RolePermission();
    rp.id = new RolePermissionId(roleId, permissionId);
    return rp;
  }
}
