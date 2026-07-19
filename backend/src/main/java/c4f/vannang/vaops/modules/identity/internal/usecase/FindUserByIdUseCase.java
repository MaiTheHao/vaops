package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUserByIdUseCase {

  private final UserQueryRepository userQueryRepository;

  public Optional<User> execute(FindByIdCommand command) {
    return userQueryRepository.findActiveById(command.userId());
  }
}
