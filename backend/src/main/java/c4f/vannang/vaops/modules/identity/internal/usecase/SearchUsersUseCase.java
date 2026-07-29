package c4f.vannang.vaops.modules.identity.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.UserSearchCriteria;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserSpecification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUsersUseCase {

  private final UserQueryRepository userQueryRepository;

  public Page<User> execute(UserSearchCriteria criteria) {
    return userQueryRepository.findAll(
        UserSpecification.search(criteria),
        criteria.toPageable()
    );
  }
}
