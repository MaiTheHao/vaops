package c4f.vannang.vaops.modules.authorization.internal.domain;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "code", nullable = false, length = 256)
  private RoleCode code;

  @Column(name = "description", nullable = true, length = 1024)
  private String description;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "deleted_at", nullable = true)
  private Instant deletedAt;

  @Column(name = "deleted_by", nullable = true)
  private UUID deletedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "created_by", nullable = true)
  private UUID createdBy;

  @Column(name = "updated_by", nullable = true)
  private UUID updatedBy;

  @OneToMany(mappedBy = "role", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private Set<RolePermission> rolePermissions = new HashSet<>();

  @OneToMany(mappedBy = "role", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private Set<UserRole> userRoles = new HashSet<>();

  public void setId(UUID id) {
    this.id = id;
  }

  public static Role create(RoleCode code, String description, UUID createdBy) {
    Role r = new Role();
    r.code = code;
    r.description = description;
    r.createdBy = createdBy;
    r.active = true;
    return r;
  }

  public void update(RoleCode code, String description, UUID updatedBy) {
    this.code = code;
    this.description = description;
    this.updatedBy = updatedBy;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }

  public void softDelete(UUID deletedByUserId) {
    this.deletedAt = Instant.now();
    this.deletedBy = deletedByUserId;
    this.active = false;
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

  public List<RolePermission> assignPermissions(List<Permission> permissions, UUID updatedBy) {
    this.updatedBy = updatedBy;
    return assignPermissions(permissions);
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

  public List<RolePermission> unassignPermissions(List<Permission> permissions, UUID updatedBy) {
    this.updatedBy = updatedBy;
    return unassignPermissions(permissions);
  }
}
