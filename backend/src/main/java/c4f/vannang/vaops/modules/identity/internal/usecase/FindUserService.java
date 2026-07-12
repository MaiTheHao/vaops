package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import c4f.vannang.vaops.modules.identity.api.dto.FindByAccountNameQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUserService {

  private final UserQueryRepository userQueryRepository;

  public Optional<User> findById(FindByIdQuery query) {
    return userQueryRepository.findActiveById(query.userId());
  }

  public Optional<User> findByAccountName(FindByAccountNameQuery query) {
    if (query.accountName() == null) return Optional.empty();
    return userQueryRepository.findActiveByAccountName(new AccountName(query.accountName()));
  }

  public Optional<User> findForAuth(FindForAuthQuery query) {
    if (query.accountName() == null) return Optional.empty();
    return userQueryRepository.findActiveByAccountName(new AccountName(query.accountName()));
  }
}
