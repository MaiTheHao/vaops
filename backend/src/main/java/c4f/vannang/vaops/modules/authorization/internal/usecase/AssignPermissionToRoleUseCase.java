package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignPermissionToRoleUseCase {

  private final RoleQueryRepository roleQueryRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final RoleWriteRepository roleWriteRepository;

  public void execute(AssignPermissionToRoleCommand command) {
    if (command == null || command.roleId() == null || command.permissionIds() == null || command.permissionIds().isEmpty()) {
      throw new ValidationException("RoleId and permissionIds must not be empty");
    }

    Role role = roleQueryRepository
        .findActiveById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found or is inactive: " + command.roleId()));

    List<UUID> permissionIdList = new ArrayList<>(command.permissionIds());
    List<Permission> permissions = permissionQueryRepository.findAllActiveByIds(permissionIdList);

    if (permissions.size() != command.permissionIds().size()) {
      throw new ResourceNotFoundException("One or more permissions were not found or are inactive");
    }

    if (role.getPermissions() == null) {
      role.setPermissions(new HashSet<>());
    }
    role.getPermissions().addAll(permissions);
    role.setUpdatedBy(command.updatedBy());

    roleWriteRepository.save(role);
  }
}
