package c4f.vannang.vaops.modules.authorization.internal.repository.spec;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.RolePermission;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import c4f.vannang.vaops.shared.specification.BaseActivatableSpecification;
import c4f.vannang.vaops.shared.specification.BaseSoftDeletableSpecification;
import c4f.vannang.vaops.shared.util.JpaSpecUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class PermissionSpecification extends BaseSoftDeletableSpecification<Permission> {

  public static Specification<Permission> hasKeyword(String keyword) {
    return keywordSearch(List.of("resource", "action", "description"), keyword);
  }

  public static Specification<Permission> hasResource(String resource) {
    return (root, query, cb) ->
        (resource == null || resource.isBlank()) ? null : cb.equal(root.get("resource").as(String.class), resource);
  }

  public static Specification<Permission> hasAction(String action) {
    return (root, query, cb) ->
        (action == null || action.isBlank()) ? null : cb.equal(root.get("action").as(String.class), action);
  }

  public static Specification<Permission> isActive(Boolean isActive) {
    return BaseActivatableSpecification.active(isActive);
  }

  public static Specification<Permission> hasRoleId(UUID roleId) {
    return (root, query, cb) -> {
      if (roleId == null) return cb.conjunction();

      query.distinct(true);

      Join<Permission, RolePermission> rolePermissions =
          JpaSpecUtil.getOrCreateJoin(root, "rolePermissions", JoinType.INNER);

      return cb.equal(rolePermissions.get("id").get("roleId"), roleId);
    };
  }

  public static Specification<Permission> hasRoleIdIn(Collection<UUID> roleIds) {
    return (root, query, cb) -> {
      if (roleIds == null) return cb.conjunction();
      if (roleIds.isEmpty()) return cb.disjunction();

      query.distinct(true);

      Join<Permission, RolePermission> rpJoin =
          JpaSpecUtil.getOrCreateJoin(root, "rolePermissions", JoinType.INNER);

      return rpJoin.get("id").get("roleId").in(roleIds);
    };
  }

  public static Specification<Permission> search(PermissionSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.<Permission>where(notDeleted());
    }
    return Specification.<Permission>where(notDeleted())
        .and(hasKeyword(criteria.keyword()))
        .and(hasResource(criteria.resource()))
        .and(hasAction(criteria.action()))
        .and(isActive(criteria.isActive()))
        .and(hasRoleIdIn(criteria.roleIds()))
        .and(createdAfter(criteria.createdFrom()))
        .and(createdBefore(criteria.createdTo()));
  }
}
