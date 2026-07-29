package c4f.vannang.vaops.modules.authorization.internal.service;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokePermissionFromRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.mapper.RoleResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

  private final RoleQueryRepository roleQueryRepository;
  private final RoleWriteRepository roleWriteRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final RoleResponseMapper roleResponseMapper;

  public RoleResponse createRole(CreateRoleCommand command) {
    if (command == null) throw new ValidationException("Command must not be null");
    RoleCode code = new RoleCode(command.code());

    if (roleQueryRepository.existsByCode(code.value())) {
      throw new ResourceAlreadyExistsException("Role code already exists");
    }

    Role role = Role.create(code, command.description(), command.createdBy());
    Role saved = roleWriteRepository.save(role);
    return roleResponseMapper.toResponse(saved);
  }

  public RoleResponse updateRole(UpdateRoleCommand command) {
    if (command == null || command.id() == null)
      throw new ValidationException("Command and ID must not be null");
    Role role = roleQueryRepository
        .findById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    RoleCode code = new RoleCode(command.code());
    role.updateInfo(code, command.description(), command.updatedBy());
    Role saved = roleWriteRepository.save(role);
    return roleResponseMapper.toResponse(saved);
  }

  public void deleteRole(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    if (!roleQueryRepository.findById(id).isPresent()) {
      throw new ResourceNotFoundException("Role not found");
    }
    roleWriteRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public RoleResponse getRoleById(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    Role role = roleQueryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    return roleResponseMapper.toResponse(role);
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> listRoles() {
    return roleQueryRepository.findAllActive().stream()
        .map(roleResponseMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> getUserRoles(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    List<Role> roles = roleQueryRepository.findActiveRolesByUserId(userId);
    return roles.stream().map(roleResponseMapper::toResponse).collect(Collectors.toList());
  }

  public void assignPermissionsToRole(AssignPermissionToRoleCommand command) {
    if (command == null
        || command.roleId() == null
        || command.permissionIds() == null
        || command.permissionIds().isEmpty()) {
      throw new ValidationException("RoleId and permissionIds must not be empty");
    }

    Role role = roleQueryRepository
        .findById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    List<Permission> permissions =
        permissionQueryRepository.findAllActiveByIds(new ArrayList<>(command.permissionIds()));
    if (permissions.size() != command.permissionIds().size()) {
      throw new ResourceNotFoundException("One or more permissions were not found");
    }

    role.assignPermissions(permissions);
    roleWriteRepository.save(role);
  }

  public void revokePermissionsFromRole(RevokePermissionFromRoleCommand command) {
    if (command == null
        || command.roleId() == null
        || command.permissionIds() == null
        || command.permissionIds().isEmpty()) {
      throw new ValidationException("RoleId and permissionIds must not be empty");
    }

    Role role = roleQueryRepository
        .findById(command.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    List<Permission> permissions =
        permissionQueryRepository.findAllActiveByIds(new ArrayList<>(command.permissionIds()));
    role.revokePermissions(permissions);
    roleWriteRepository.save(role);
  }
}
