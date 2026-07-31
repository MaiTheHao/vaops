package c4f.vannang.vaops.modules.identity.internal.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.UserSearchCriteria;

public final class UserSpecification {

    private UserSpecification() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Specification<User> isNotDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> isActive(Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), isActive);
        };
    }

    public static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("accountName").as(String.class)), pattern),
                    cb.like(cb.lower(root.get("displayName").as(String.class)), pattern)
            );
        };
    }

    public static Specification<User> search(UserSearchCriteria criteria) {
        return Specification.where(isNotDeleted())
                .and(isActive(criteria.getIsActive()))
                .and(hasKeyword(criteria.getKeyword()));
    }
}
