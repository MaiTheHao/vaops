package c4f.vannang.vaops.modules.authorization.domain.id;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.EqualsAndHashCode;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserRoleId implements Serializable {

  @Column(name = "user_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID userId;

  @Column(name = "role_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID roleId;
}
