package c4f.vannang.vaops.modules.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;

@Service
@RequiredArgsConstructor
@Transactional
public class ChangePasswordService {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  public void execute(UUID userId, String oldRawPassword, String newRawPassword) {
    if (newRawPassword == null || newRawPassword.length() < 8) {
      throw new ValidationException("New password must be at least 8 characters long");
    }

    User user = userQueryRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(oldRawPassword, user.getPasswordHash())) {
      throw new BusinessRuleViolationException("Invalid old password");
    }

    user.changePassword(passwordEncoder.encode(newRawPassword));
    userWriteRepository.save(user);
  }
}
