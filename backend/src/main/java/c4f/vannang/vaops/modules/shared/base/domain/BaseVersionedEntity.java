package c4f.vannang.vaops.modules.shared.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseVersionedEntity extends BaseSoftDeletableEntity {

  @Version
  @Column(name = "version", nullable = false)
  private Integer version = 0;
}
