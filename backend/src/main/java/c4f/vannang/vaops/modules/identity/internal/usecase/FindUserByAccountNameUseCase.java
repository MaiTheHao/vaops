package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUserByAccountNameUseCase {

  private final UserQueryRepository userQueryRepository;

  public Optional<User> execute(FindByAccountNameCommand command) {
    if (command.accountName() == null) return Optional.empty();
    return userQueryRepository.findByAccountName(new AccountName(command.accountName()));
  }
}
