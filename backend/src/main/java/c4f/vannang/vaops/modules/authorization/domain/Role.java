package c4f.vannang.vaops.modules.authorization.domain;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import org.hibernate.annotations.UuidGenerator;

@Table(name = "roles")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

  @Id
  @UuidGenerator
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "code", nullable = false, unique = true, length = 256)
  private String code;

  @Column(name = "description", nullable = true, length = 1024)
  private String description;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @Column(name = "deleted_at", nullable = true)
  @ManyToMany

  private Instant deletedAt;

  @Column(name = "deleted_by", nullable = true)
  private UUID deletedBy;

  @Column(name = "created_by", nullable = true)
  private UUID createdBy;

  @Column(name = "updated_by", nullable = true)
  private UUID updatedBy;

  @ManyToMany
  @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
  private Set<Permission> permissions;
}
