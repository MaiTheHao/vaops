package c4f.vannang.vaops.modules.authorization.internal.repository.spec;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.time.Instant;
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
          cb.like(cb.lower(root.get("description")), pattern)
      );
    };
  }

  public static Specification<Permission> hasResource(String resource) {
    return (root, query, cb) -> (resource == null || resource.isBlank()) ? null : cb.equal(root.get("resource"), resource);
  }

  public static Specification<Permission> hasAction(String action) {
    return (root, query, cb) -> (action == null || action.isBlank()) ? null : cb.equal(root.get("action"), action);
  }

  public static Specification<Permission> isActive(Boolean isActive) {
    return (root, query, cb) -> isActive == null ? null : cb.equal(root.get("isActive"), isActive);
  }

  public static Specification<Permission> isNotDeleted() {
    return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
  }

  public static Specification<Permission> createdAfter(Instant from) {
    return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
  }

  public static Specification<Permission> createdBefore(Instant to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
  }

  public static Specification<Permission> hasRoleId(UUID roleId) {
    return (root, query, cb) -> {
      if (roleId == null) return cb.conjunction();
      Subquery<UUID> subquery = query.subquery(UUID.class);
      Root<Role> roleRoot = subquery.from(Role.class);
      Join<Role, Permission> joinedPermission = roleRoot.join("permissions");
      subquery.select(joinedPermission.get("id"))
              .where(
                  cb.equal(roleRoot.get("id"), roleId),
                  cb.isTrue(roleRoot.get("isActive")),
                  cb.isNull(roleRoot.get("deletedAt"))
              );
      return root.get("id").in(subquery);
    };
  }

  public static Specification<Permission> hasUserId(UUID userId) {
    return (root, query, cb) -> {
      if (userId == null) return cb.conjunction();
      Subquery<UUID> subquery = query.subquery(UUID.class);
      Root<Role> roleRoot = subquery.from(Role.class);
      Join<Role, Permission> joinedPermission = roleRoot.join("permissions");
      Root<UserRole> userRoleRoot = subquery.from(UserRole.class);
      subquery.select(joinedPermission.get("id"))
              .where(
                  cb.equal(roleRoot.get("id"), userRoleRoot.get("id").get("roleId")),
                  cb.equal(userRoleRoot.get("id").get("userId"), userId),
                  cb.isNull(userRoleRoot.get("revokedAt")),
                  cb.isTrue(roleRoot.get("isActive")),
                  cb.isNull(roleRoot.get("deletedAt"))
              );
      return root.get("id").in(subquery);
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
        .and(hasRoleId(criteria.roleId()))
        .and(hasUserId(criteria.userId()))
        .and(createdAfter(criteria.createdFrom()))
        .and(createdBefore(criteria.createdTo()));
  }
}
