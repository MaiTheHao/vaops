package c4f.vannang.vaops.modules.authorization.internal.domain;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.shared.base.domain.BaseVersionedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import c4f.vannang.vaops.shared.base.domain.Activatable;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseVersionedEntity implements Activatable {

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

  @Column(name = "code", nullable = false, length = 256)
  private RoleCode code;

  @Column(name = "description", nullable = true, length = 1024)
  private String description;

  @OneToMany(mappedBy = "role", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private Set<RolePermission> rolePermissions = new HashSet<>();

  @OneToMany(mappedBy = "role", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private Set<UserRole> userRoles = new HashSet<>();

  public static Role create(RoleCode code, String description) {
    Role r = new Role();
    r.code = code;
    r.description = description;
    return r;
  }

  public void update(RoleCode code, String description) {
    this.code = code;
    this.description = description;
  }

  public RolePermission assignPermission(Permission permission) {
    RolePermission rp = RolePermission.create(this, permission);
    this.rolePermissions.add(rp);
    return rp;
  }

  public List<RolePermission> assignPermissions(List<Permission> permissions) {
    return permissions.stream()
        .map(this::assignPermission)
        .toList();
  }

  public RolePermission unassignPermission(Permission permission) {
    RolePermission rp = this.rolePermissions.stream()
        .filter(r -> r.getPermission().getId().equals(permission.getId()))
        .findFirst()
        .orElse(null);

    if (rp != null) this.rolePermissions.remove(rp);
    
    return rp;
  }

  public List<RolePermission> unassignPermissions(List<Permission> permissions) {
    List<RolePermission> removed = new ArrayList<>();

    for (Permission p : permissions) {
      RolePermission rp = unassignPermission(p);
      if (rp != null) removed.add(rp);
    }
    
    return removed;
  }
}
