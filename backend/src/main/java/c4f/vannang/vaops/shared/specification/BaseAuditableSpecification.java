package c4f.vannang.vaops.shared.specification;

import c4f.vannang.vaops.shared.base.BaseAuditableEntity;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

/**
 * Base specification for auditable entities, providing the shared {@code createdAt} range filters.
 *
 * @param <T> the auditable entity type
 */
public class BaseAuditableSpecification<T extends BaseAuditableEntity>
    extends BaseSpecification<T> {

  protected BaseAuditableSpecification() {}

  public static <T extends BaseAuditableEntity> Specification<T> createdAfter(Instant from) {
    return (root, query, cb) ->
        from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static <T extends BaseAuditableEntity> Specification<T> createdBefore(Instant to) {
    return (root, query, cb) ->
        to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }
}
