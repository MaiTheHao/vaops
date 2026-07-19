package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProfileUseCase {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;

  public void execute(UpdateProfileCommand command) {
    User user = userQueryRepository.findActiveById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    DisplayName dn = command.displayName() != null ? new DisplayName(command.displayName()) : null;
    AvatarUrl au = command.avatarUrl() != null ? new AvatarUrl(command.avatarUrl()) : null;

    user.updateProfile(dn, au);
    userWriteRepository.save(user);
  }
}
