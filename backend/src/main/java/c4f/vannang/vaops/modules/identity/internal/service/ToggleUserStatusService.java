package c4f.vannang.vaops.modules.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ToggleUserStatusService {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;

  public void execute(UUID userId, boolean active) {
    User user = userQueryRepository.findActiveById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (active) {
      user.activate();
    } else {
      user.deactivate();
    }

    userWriteRepository.save(user);
  }
}
