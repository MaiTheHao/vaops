package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RevokeRoleFromUserUseCase {

  private final UserRoleQueryRepository userRoleQueryRepository;
  private final UserRoleWriteRepository userRoleWriteRepository;

  public void execute(RevokeRoleFromUserCommand command) {
    if (command == null || command.userId() == null || command.roleId() == null) {
      throw new ValidationException("UserId and roleId must not be null");
    }

    UserRoleId userRoleId = new UserRoleId(command.userId(), command.roleId());
    UserRole userRole = userRoleQueryRepository
        .findById(userRoleId)
        .orElseThrow(() -> new ResourceNotFoundException("Role assignment not found for user: " + command.userId()));

    if (userRole.getRevokedAt() == null) {
      userRole.setRevokedAt(Instant.now());
      userRole.setRevokedBy(command.revokedBy());
      userRoleWriteRepository.save(userRole);
    }
  }
}
