package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangePasswordUseCase {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  public void execute(ChangePasswordCommand command) {
    User.validatePasswordStrength(command.newPassword());

    User user = userQueryRepository.findActiveById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(command.oldPassword(), user.getPasswordHash().value())) {
      throw new BusinessRuleViolationException("Invalid old password");
    }

    user.changePassword(new PasswordHash(passwordEncoder.encode(command.newPassword())));
    userWriteRepository.save(user);
  }
}
