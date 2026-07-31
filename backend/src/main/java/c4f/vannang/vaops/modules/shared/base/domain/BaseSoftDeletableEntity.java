package c4f.vannang.vaops.modules.shared.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSoftDeletableEntity extends BaseAuditableEntity {

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "deleted_by")
  private UUID deletedBy;

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
