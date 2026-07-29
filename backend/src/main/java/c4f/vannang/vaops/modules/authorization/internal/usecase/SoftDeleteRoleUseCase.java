package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.SoftDeleteRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SoftDeleteRoleUseCase {

  private final RoleQueryRepository roleQueryRepository;
  private final RoleWriteRepository roleWriteRepository;

  public void execute(SoftDeleteRoleCommand command) {
    if (command == null || command.roleId() == null) {
      throw new ValidationException("SoftDeleteRoleCommand and roleId must not be null");
    }

    Role role = roleQueryRepository
        .findActiveById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found or is already deleted: " + command.roleId()));

    Instant now = Instant.now();
    role.setDeletedAt(now);
    role.setDeletedBy(command.deletedBy());
    role.setIsActive(false);

    roleWriteRepository.save(role);
  }
}
