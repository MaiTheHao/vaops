package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;

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

  @ManyToMany
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id")
  )
  private Set<Permission> permissions = new HashSet<>();

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

  public void updateInfo(RoleCode code, String description, UUID updatedBy) {
    this.code = code;
    this.description = description;
    this.updatedBy = updatedBy;
  }

  public void assignPermission(Permission permission) {
    if (permission != null && permission.isActive()) {
      this.permissions.add(permission);
    }
  }

  public void assignPermissions(Collection<Permission> newPermissions) {
    if (newPermissions != null) {
      newPermissions.stream()
          .filter(Permission::isActive)
          .forEach(this.permissions::add);
    }
  }

  public void revokePermission(Permission permission) {
    if (permission != null) {
      this.permissions.remove(permission);
    }
  }

  public void revokePermissions(Collection<Permission> permissionsToRevoke) {
    if (permissionsToRevoke != null) {
      this.permissions.removeAll(permissionsToRevoke);
    }
  }

  public boolean hasPermission(PermissionResource resource, PermissionAction action) {
    if (!this.active) return false;
    return this.permissions.stream()
        .anyMatch(p -> p.isActive() 
                    && p.getResource().equals(resource) 
                    && p.getAction().equals(action));
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
}
