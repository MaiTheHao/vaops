package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import jakarta.persistence.Column;
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
@Table(name = "user_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

  @EmbeddedId
  private UserRoleId id;

  @ManyToOne
  @MapsId("roleId")
  @JoinColumn(name = "role_id")
  private Role role;

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  @Column(name = "assigned_by")
  private UUID assignedBy;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "revoked_by")
  private UUID revokedBy;

  public void revoke(UUID revokedBy) {
    this.revokedAt = Instant.now();
    this.revokedBy = revokedBy;
  }

  public static UserRole create(UUID userId, Role role, UUID assignedBy) {
    UserRole ur = new UserRole();
    ur.id = new UserRoleId(userId, role.getId());
    ur.role = role;
    ur.assignedAt = Instant.now();
    ur.assignedBy = assignedBy;
    return ur;
  }
}
