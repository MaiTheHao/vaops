package c4f.vannang.vaops.modules.authorization.internal.repository.spec;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification {

  public static Specification<Role> hasKeyword(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) return null;
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("code")), pattern),
          cb.like(cb.lower(root.get("description")), pattern));
    };
  }

  public static Specification<Role> hasCode(String code) {
    return (root, query, cb) ->
        (code == null || code.isBlank()) ? null : cb.equal(root.get("code"), code);
  }

  public static Specification<Role> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("active"), isActive);
  }

  public static Specification<Role> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }

  public static Specification<Role> createdAfter(Instant from) {
    return (root, query, cb) ->
        from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<Role> createdBefore(Instant to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<Role> hasUserId(UUID userId) {
    return (root, query, cb) -> {
      if (userId == null) return cb.conjunction();

      query.distinct(true);

      Join<Role, UserRole> userRoles = getOrCreateJoin(root, "userRoles", JoinType.INNER);

      return cb.equal(userRoles.get("id").get("userId"), userId);
    };
  }

  public static Specification<Role> search(RoleSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.where(isNotDeleted());
    }
    return Specification.where(isNotDeleted())
        .and(hasKeyword(criteria.keyword()))
        .and(hasCode(criteria.code()))
        .and(isActive(criteria.isActive()))
        .and(hasUserId(criteria.userId()))
        .and(createdAfter(criteria.createdFrom()))
        .and(createdBefore(criteria.createdTo()));
  }

  // Will move into utils
  @SuppressWarnings("unchecked")
  private static <Z, X> Join<Z, X> getOrCreateJoin(
      Root<Z> root, String attribute, JoinType joinType) {
    Set<Join<Z, ?>> rootJoins = root.getJoins();

    for (var join : rootJoins) {
      if (join.getAttribute().getName().equals(attribute)) return (Join<Z, X>) join;
    }

    return root.join(attribute, joinType);
  }
}
