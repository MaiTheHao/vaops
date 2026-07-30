package c4f.vannang.vaops.modules.authorization.internal.domain;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.RolePermissionId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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

  @ManyToOne
  @MapsId("roleId")
  @JoinColumn(name = "role_id")
  private Role role;

  @ManyToOne
  @MapsId("permissionId")
  @JoinColumn(name = "permission_id")
  private Permission permission;

  public static RolePermission create(Role role, Permission permission) {
    RolePermission rp = new RolePermission();
    rp.id = new RolePermissionId(role.getId(), permission.getId());
    rp.role = role;
    rp.permission = permission;
    return rp;
  }
}
