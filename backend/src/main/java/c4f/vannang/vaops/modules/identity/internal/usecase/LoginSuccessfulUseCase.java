package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginSuccessfulUseCase {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;

  public void execute(RecordSuccessfulLoginCommand command) {
    User user = userQueryRepository.findActiveById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.recordSuccessfulLogin();
    userWriteRepository.save(user);
  }
}
