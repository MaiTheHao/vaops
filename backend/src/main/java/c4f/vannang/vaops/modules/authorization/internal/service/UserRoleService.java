package c4f.vannang.vaops.modules.authorization.internal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRoleToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.mapper.PermissionResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.mapper.RoleResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

  private final RoleQueryRepository roleQueryRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final UserRoleQueryRepository userRoleQueryRepository;
  private final UserRoleWriteRepository userRoleWriteRepository;
  private final RoleResponseMapper roleResponseMapper;
  private final PermissionResponseMapper permissionResponseMapper;

  public void assignRolesToUser(AssignRoleToUserCommand command) {
    if (command == null || command.userId() == null || command.roleIds() == null || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }

    List<UUID> roleIdList = new ArrayList<>(command.roleIds());
    List<Role> activeRoles = roleQueryRepository.findAllActiveByIds(roleIdList);
    if (activeRoles.size() != command.roleIds().size()) {
      throw new ResourceNotFoundException("One or more roles were not found");
    }

    List<UserRole> toSave = roleIdList.stream()
        .map(roleId -> UserRole.assign(command.userId(), roleId, command.assignedBy()))
        .collect(Collectors.toList());

    userRoleWriteRepository.saveAll(toSave);
  }

  public void revokeRolesFromUser(RevokeRoleFromUserCommand command) {
    if (command == null || command.userId() == null || command.roleIds() == null || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }
    userRoleWriteRepository.deleteByUserIdAndRoleIdIn(command.userId(), command.roleIds());
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> getUserRoles(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    List<Role> roles = roleQueryRepository.findActiveRolesByUserId(userId);
    return roles.stream().map(roleResponseMapper::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> getUserPermissions(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    List<Permission> permissions = permissionQueryRepository.findActivePermissionsByUserId(userId);
    return permissions.stream().map(permissionResponseMapper::toResponse).collect(Collectors.toList());
  }
}
