package c4f.vannang.vaops.shared.specification;

import c4f.vannang.vaops.shared.base.Activatable;
import org.springframework.data.jpa.domain.Specification;

public interface BaseActivatableSpecification<T extends Activatable> {

  static <T extends Activatable> Specification<T> active(Boolean isActive) {
    return (root, query, cb) ->
        isActive == null ? cb.conjunction() : cb.equal(root.get("active"), isActive);
  }
}
