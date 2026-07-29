package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.UserRoleSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class UserRoleSpecification {

  public static Specification<UserRole> hasUserId(UUID userId) {
    return (root, query, cb) -> userId == null ? null : cb.equal(root.get("id").get("userId"), userId);
  }

  public static Specification<UserRole> hasRoleId(UUID roleId) {
    return (root, query, cb) -> roleId == null ? null : cb.equal(root.get("id").get("roleId"), roleId);
  }

  public static Specification<UserRole> hasRoleIdsIn(List<UUID> roleIds) {
    return (root, query, cb) -> (roleIds == null || roleIds.isEmpty()) ? null : root.get("id").get("roleId").in(roleIds);
  }

  public static Specification<UserRole> isNotRevoked() {
    return (root, query, cb) -> cb.isNull(root.get("revokedAt"));
  }

  public static Specification<UserRole> isRevoked(Boolean revoked) {
    if (revoked == null) return null;
    return revoked ? (root, query, cb) -> cb.isNotNull(root.get("revokedAt")) : isNotRevoked();
  }

  public static Specification<UserRole> search(UserRoleSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.where(isNotRevoked());
    }
    return Specification.where(hasUserId(criteria.userId()))
        .and(hasRoleId(criteria.roleId()))
        .and(hasRoleIdsIn(criteria.roleIds()))
        .and(isRevoked(criteria.isRevoked()));
  }
}
