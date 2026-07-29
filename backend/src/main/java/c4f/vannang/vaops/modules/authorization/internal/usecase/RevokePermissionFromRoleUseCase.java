package c4f.vannang.vaops.modules.authorization.internal.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokePermissionFromRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RevokePermissionFromRoleUseCase {

  private final RoleQueryRepository roleQueryRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final RoleWriteRepository roleWriteRepository;

  public void execute(RevokePermissionFromRoleCommand command) {
    if (command == null || command.roleId() == null || command.permissionId() == null) {
      throw new ValidationException("RoleId and permissionId must not be null");
    }

    Role role = roleQueryRepository
        .findActiveById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found or is inactive: " + command.roleId()));

    Permission permission = permissionQueryRepository
        .findActiveById(command.permissionId())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found or is inactive: " + command.permissionId()));

    if (role.getPermissions() != null) {
      role.getPermissions().remove(permission);
    }
    role.setUpdatedBy(command.updatedBy());

    roleWriteRepository.save(role);
  }
}
