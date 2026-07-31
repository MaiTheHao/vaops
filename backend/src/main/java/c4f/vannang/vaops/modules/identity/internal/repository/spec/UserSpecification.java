package c4f.vannang.vaops.modules.identity.internal.repository.spec;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.UserSearchCriteria;
import c4f.vannang.vaops.shared.specification.BaseActivatableSpecification;
import c4f.vannang.vaops.shared.specification.BaseSoftDeletableSpecification;

public final class UserSpecification extends BaseSoftDeletableSpecification<User> {

    private UserSpecification() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Specification<User> isActive(Boolean isActive) {
        return BaseActivatableSpecification.active(isActive);
    }

    public static Specification<User> hasKeyword(String keyword) {
        return keywordSearch(List.of("accountName", "displayName"), keyword);
    }

    public static Specification<User> search(UserSearchCriteria criteria) {
        return Specification.<User>where(notDeleted())
                .and(isActive(criteria.getIsActive()))
                .and(hasKeyword(criteria.getKeyword()));
    }
}
