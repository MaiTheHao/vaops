package c4f.vannang.vaops.modules.authorization.internal.domain;

import java.time.Instant;
import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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

  @Column(name = "assigned_at", nullable = false)
  private Instant assignedAt;

  @Column(name = "assigned_by")
  private UUID assignedBy;

  public static UserRole assign(UUID userId, UUID roleId, UUID assignedBy) {
    UserRole ur = new UserRole();
    ur.id = new UserRoleId(userId, roleId);
    ur.assignedAt = Instant.now();
    ur.assignedBy = assignedBy;
    return ur;
  }
}
