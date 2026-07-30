package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "resource", nullable = false, length = 256)
  private PermissionResource resource;

  @Column(name = "action", nullable = false, length = 256)
  private PermissionAction action;

  @Column(name = "description", nullable = true, length = 1024)
  private PermissionDescription description;

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

  @OneToMany(mappedBy = "permission", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
  private Set<RolePermission> rolePermissions = new HashSet<>();

  public void setId(UUID id) {
    this.id = id;
  }

  public static Permission create(
      PermissionResource resource,
      PermissionAction action,
      PermissionDescription description,
      UUID createdBy) {
    Permission p = new Permission();
    p.resource = resource;
    p.action = action;
    p.description = description;
    p.createdBy = createdBy;
    p.active = true;
    return p;
  }

  public void update(
      PermissionResource resource,
      PermissionAction action,
      PermissionDescription description,
      UUID updatedBy) {
    this.resource = resource;
    this.action = action;
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
}
