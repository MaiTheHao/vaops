package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.shared.base.Activatable;
import c4f.vannang.vaops.shared.base.BaseSoftDeletableEntity;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseSoftDeletableEntity implements Activatable {

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Override
  public boolean isActive() {
    return active;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  @Column(name = "resource", nullable = false, length = 256)
  private PermissionResource resource;

  @Column(name = "action", nullable = false, length = 256)
  private PermissionAction action;

  @Column(name = "description", nullable = true, length = 1024)
  private PermissionDescription description;

  @OneToMany(mappedBy = "permission", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private Set<RolePermission> rolePermissions = new HashSet<>();

  public static Permission create(
      PermissionResource resource,
      PermissionAction action,
      PermissionDescription description) {
    Permission p = new Permission();
    p.resource = resource;
    p.action = action;
    p.description = description;
    return p;
  }

  public void update(
      PermissionResource resource,
      PermissionAction action,
      PermissionDescription description) {
    this.resource = resource;
    this.action = action;
    this.description = description;
  }
}
