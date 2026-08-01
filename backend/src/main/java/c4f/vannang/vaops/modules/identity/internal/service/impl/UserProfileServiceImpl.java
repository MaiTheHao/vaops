package c4f.vannang.vaops.modules.identity.internal.service.impl;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public User getProfile(FindByIdCommand command) {
    return userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Override
  public User updateProfile(UpdateProfileCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    DisplayName dn = command.displayName() != null ? new DisplayName(command.displayName()) : null;
    AvatarUrl au = command.avatarUrl() != null ? new AvatarUrl(command.avatarUrl()) : null;

    user.updateProfile(dn, au);
    return userWriteRepository.save(user);
  }

  @Override
  public void changePassword(ChangePasswordCommand command) {
    User.validatePasswordStrength(command.newPassword());

    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(command.oldPassword(), user.getPasswordHash().value())) {
      throw new BusinessRuleViolationException("Invalid old password");
    }

    user.changePassword(new PasswordHash(passwordEncoder.encode(command.newPassword())));
    userWriteRepository.save(user);
  }
}
