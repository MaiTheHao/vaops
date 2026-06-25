package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Table(name = "user_roles")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserRole {

  @EmbeddedId
  @EqualsAndHashCode.Include
  private UserRoleId id;

  @Column(name = "assigned_at", nullable = false)
  @Builder.Default
  private Instant assignedAt = Instant.now();

  @Column(name = "assigned_by")
  private UUID assignedBy;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "revoked_by")
  private UUID revokedBy;
}
