package c4f.vannang.vaops.modules.authorization.internal.repository.spec;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleSearchCriteria;
import c4f.vannang.vaops.shared.specification.BaseActivatableSpecification;
import c4f.vannang.vaops.shared.specification.BaseSoftDeletableSpecification;
import c4f.vannang.vaops.shared.util.JpaSpecUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification extends BaseSoftDeletableSpecification<Role> {

  public static Specification<Role> hasKeyword(String keyword) {
    return keywordSearch(List.of("code", "description"), keyword);
  }

  public static Specification<Role> hasCode(String code) {
    return (root, query, cb) ->
        (code == null || code.isBlank()) ? null : cb.equal(root.get("code").as(String.class), code);
  }

  public static Specification<Role> isActive(Boolean isActive) {
    return BaseActivatableSpecification.active(isActive);
  }

  public static Specification<Role> hasUserId(UUID userId) {
    return (root, query, cb) -> {
      if (userId == null) return cb.conjunction();

      query.distinct(true);

      Join<Role, UserRole> userRoles = JpaSpecUtil.getOrCreateJoin(root, "userRoles", JoinType.INNER);

      return cb.equal(userRoles.get("id").get("userId"), userId);
    };
  }

  public static Specification<Role> search(RoleSearchCriteria criteria) {
    if (criteria == null) {
      return Specification.<Role>where(notDeleted());
    }
    return Specification.<Role>where(notDeleted())
        .and(hasKeyword(criteria.keyword()))
        .and(hasCode(criteria.code()))
        .and(isActive(criteria.isActive()))
        .and(hasUserId(criteria.userId()))
        .and(createdAfter(criteria.createdFrom()))
        .and(createdBefore(criteria.createdTo()));
  }
}
