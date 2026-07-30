package c4f.vannang.vaops.modules.authorization.internal.repository.spec;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.RolePermission;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class PermissionSpecification {

  public static Specification<Permission> hasKeyword(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) return null;
      String pattern = "%" + keyword.trim().toLowerCase() + "%";
      return cb.or(
          cb.like(cb.lower(root.get("resource")), pattern),
          cb.like(cb.lower(root.get("action")), pattern),
          cb.like(cb.lower(root.get("description")), pattern));
    };
  }

  public static Specification<Permission> hasResource(String resource) {
    return (root, query, cb) ->
        (resource == null || resource.isBlank()) ? null : cb.equal(root.get("resource"), resource);
  }

  public static Specification<Permission> hasAction(String action) {
    return (root, query, cb) ->
        (action == null || action.isBlank()) ? null : cb.equal(root.get("action"), action);
  }

  public static Specification<Permission> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("active"), isActive);
  }

  public static Specification<Permission> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }

  public static Specification<Permission> createdAfter(Instant from) {
    return (root, query, cb) ->
        from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<Permission> createdBefore(Instant to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<Permission> hasRoleId(UUID roleId) {
    return (root, query, cb) -> {
      if (roleId == null) return cb.conjunction();

      query.distinct(true);

      Join<Permission, RolePermission> rolePermissions =
          getOrCreateJoin(root, "rolePermissions", JoinType.INNER);

      return cb.equal(rolePermissions.get("id").get("roleId"), roleId);
    };
  }

  public static Specification<Permission> hasRoleIdIn(Collection<UUID> roleIds) {
    return (root, query, cb) -> {
      if (roleIds == null) return cb.conjunction();
      if (roleIds.isEmpty()) return cb.disjunction();

      query.distinct(true);

      Join<Permission, RolePermission> rpJoin =
          getOrCreateJoin(root, "rolePermissions", JoinType.INNER);

      return rpJoin.get("id").get("roleId").in(roleIds);
    };
  }

  public static Specification<Permission> search(PermissionSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.where(isNotDeleted());
    }
    return Specification.where(isNotDeleted())
        .and(hasKeyword(criteria.keyword()))
        .and(hasResource(criteria.resource()))
        .and(hasAction(criteria.action()))
        .and(isActive(criteria.isActive()))
        .and(hasRoleIdIn(criteria.roleIds()))
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
