package c4f.vannang.vaops.shared.specification;

import c4f.vannang.vaops.shared.base.BaseSoftDeletableEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * Base specification for soft-deletable entities.
 *
 * @param <T> the soft-deletable entity type
 */
public class BaseSoftDeletableSpecification<T extends BaseSoftDeletableEntity>
    extends BaseAuditableSpecification<T> {

  protected BaseSoftDeletableSpecification() {}

  public static <T extends BaseSoftDeletableEntity> Specification<T> notDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }
}
