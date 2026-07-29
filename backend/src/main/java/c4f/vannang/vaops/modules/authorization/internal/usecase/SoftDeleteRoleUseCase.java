package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
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

  public void execute(UUID roleId, UUID deletedBy) {
    if (roleId == null) {
      throw new ValidationException("RoleId must not be null");
    }

    Role role = roleQueryRepository
        .findActiveById(roleId)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found or is already deleted: " + roleId));

    role.setDeletedAt(Instant.now());
    role.setDeletedBy(deletedBy);
    role.setIsActive(false);

    roleWriteRepository.save(role);
  }
}
