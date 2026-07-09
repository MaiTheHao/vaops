package c4f.vannang.vaops.modules.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangePasswordService {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  public void execute(UUID userId, String oldRawPassword, String newRawPassword) {
    User.validatePasswordStrength(newRawPassword);

    User user = userQueryRepository.findActiveById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(oldRawPassword, user.getPasswordHash().value())) {
      throw new BusinessRuleViolationException("Invalid old password");
    }

    user.changePassword(new PasswordHash(passwordEncoder.encode(newRawPassword)));
    userWriteRepository.save(user);
  }
}
