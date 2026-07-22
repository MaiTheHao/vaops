package c4f.vannang.vaops.modules.identity.internal.usecase;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetProfileUseCase {

  private final UserQueryRepository userQueryRepository;

  public User execute(FindByIdCommand command) {
    return userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }
}

