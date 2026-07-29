package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.SoftDeletePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SoftDeletePermissionUseCase {

  private final PermissionQueryRepository permissionQueryRepository;
  private final PermissionWriteRepository permissionWriteRepository;

  public void execute(SoftDeletePermissionCommand command) {
    if (command == null || command.permissionId() == null) {
      throw new ValidationException("SoftDeletePermissionCommand and permissionId must not be null");
    }

    Permission permission = permissionQueryRepository
        .findActiveById(command.permissionId())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found or is already deleted: " + command.permissionId()));

    Instant now = Instant.now();
    permission.setDeletedAt(now);
    permission.setDeletedBy(command.deletedBy());
    permission.setIsActive(false);

    permissionWriteRepository.save(permission);
  }
}
