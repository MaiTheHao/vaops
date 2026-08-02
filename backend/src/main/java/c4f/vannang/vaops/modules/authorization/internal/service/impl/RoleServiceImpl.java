package c4f.vannang.vaops.modules.authorization.internal.service.impl;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionsToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokePermissionFromRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.spec.RoleSpecification;
import c4f.vannang.vaops.modules.authorization.internal.service.RoleService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

  private final RoleQueryRepository roleQueryRepository;
  private final RoleWriteRepository roleWriteRepository;
  private final PermissionQueryRepository permissionQueryRepository;

  @Override
  public Role createRole(CreateRoleCommand command) {
    if (command == null) throw new ValidationException("Command must not be null");
    RoleCode code = new RoleCode(command.code());

    if (roleQueryRepository.existsByCode(code)) {
      throw new ResourceAlreadyExistsException("Role code already exists");
    }

    Role role = Role.create(code, command.description());

    if (command.permissionIds() != null && !command.permissionIds().isEmpty()) {
      List<Permission> permissions =
          permissionQueryRepository.findAllActiveByIdIn(new ArrayList<>(command.permissionIds()));
      if (permissions.size() != command.permissionIds().size()) {
        throw new ResourceNotFoundException("One or more permissions were not found");
      }
      role.assignPermissions(permissions);
    }

    return roleWriteRepository.save(role);
  }

  @Override
  public Role updateRole(UpdateRoleCommand command) {
    if (command == null || command.id() == null)
      throw new ValidationException("Command and ID must not be null");
    Role role = roleQueryRepository
        .findById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    role.update(new RoleCode(command.code()), command.description());
    return roleWriteRepository.save(role);
  }

  @Override
  public void softDeleteRole(UUID id, UUID deletedBy) {
    if (id == null) throw new ValidationException("ID must not be null");
    Role role = roleQueryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

    role.softDelete(deletedBy);
    roleWriteRepository.save(role);
  }

  @Override
  public void hardDeleteRole(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    if (!roleQueryRepository.existsByIdWithDeleted(id)) {
      throw new ResourceNotFoundException("Role not found");
    }
    roleWriteRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Role getRoleById(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    return roleQueryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Role> getRolesByUserId(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    return roleQueryRepository.findAllActiveByUserId(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<Role> searchRoles(RoleSearchCriteria criteria) {
    Page<Role> rolePage =
        roleQueryRepository.findAll(RoleSpecification.search(criteria),
            criteria != null ? criteria.toPageable() : Pageable.unpaged());
    return PageResponse.from(rolePage, role -> role);
  }

  @Override
  public void assignPermissionsToRole(AssignPermissionsToRoleCommand command) {
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
        permissionQueryRepository.findAllActiveByIdIn(new ArrayList<>(command.permissionIds()));
    if (permissions.size() != command.permissionIds().size()) {
      throw new ResourceNotFoundException("One or more permissions were not found");
    }

    role.assignPermissions(permissions);
    roleWriteRepository.save(role);
  }

  @Override
  public void unassignPermissionsFromRole(RevokePermissionFromRoleCommand command) {
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
        permissionQueryRepository.findAllActiveByIdIn(new ArrayList<>(command.permissionIds()));
    if (permissions.size() != command.permissionIds().size()) {
      throw new ResourceNotFoundException("One or more permissions were not found");
    }

    role.unassignPermissions(permissions);
    roleWriteRepository.save(role);
  }
}
