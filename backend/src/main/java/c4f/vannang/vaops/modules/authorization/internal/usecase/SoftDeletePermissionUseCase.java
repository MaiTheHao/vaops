package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
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

  public void execute(UUID permissionId, UUID deletedBy) {
    if (permissionId == null) {
      throw new ValidationException("PermissionId must not be null");
    }

    Permission permission = permissionQueryRepository
        .findActiveById(permissionId)
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found or is already deleted: " + permissionId));

    permission.setDeletedAt(Instant.now());
    permission.setDeletedBy(deletedBy);
    permission.setIsActive(false);

    permissionWriteRepository.save(permission);
  }
}
