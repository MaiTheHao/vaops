package c4f.vannang.vaops.shared.specification;

import jakarta.persistence.criteria.Predicate;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Base class holding generic JPA criteria specification helpers.
 *
 * @param <T> the entity type the specification applies to (used for subclassing only)
 */
public class BaseSpecification<T> {

  protected BaseSpecification() {}

  public static <T> Specification<T> idEquals(UUID id) {
    return (root, query, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("id"), id);
  }

  public static <T> Specification<T> idIn(Collection<UUID> ids) {
    return (root, query, cb) -> {
      if (ids == null || ids.isEmpty()) {
        return cb.disjunction();
      }
      return root.get("id").in(ids);
    };
  }

  public static <T> Specification<T> keywordLike(String field, String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.like(cb.lower(root.get(field).as(String.class)), pattern);
    };
  }

  public static <T> Specification<T> keywordSearch(Collection<String> fields, String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      Predicate[] predicates = fields.stream()
          .map(field -> cb.like(cb.lower(root.get(field).as(String.class)), pattern))
          .toArray(Predicate[]::new);
      return cb.or(predicates);
    };
  }
}
