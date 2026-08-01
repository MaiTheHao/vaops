package c4f.vannang.vaops.modules.authorization.internal.service.impl;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRolesToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleWriteRepository;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
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
public class UserRoleServiceImpl implements UserRoleService {

  private final RoleQueryRepository roleQueryRepository;
  private final UserRoleWriteRepository userRoleWriteRepository;

  @Override
  public void assignRolesToUser(AssignRolesToUserCommand command) {
    if (command == null
        || command.userId() == null
        || command.roleIds() == null
        || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }

    List<UUID> roleIdList = new ArrayList<>(command.roleIds());
    List<Role> activeRoles = roleQueryRepository.findAllActiveByIdIn(roleIdList);
    if (activeRoles.size() != command.roleIds().size()) {
      throw new ResourceNotFoundException("One or more roles were not found");
    }

    List<UserRole> toSave = activeRoles.stream()
        .map(role -> UserRole.create(command.userId(), role, command.assignedBy()))
        .collect(Collectors.toList());

    userRoleWriteRepository.saveAll(toSave);
  }

  @Override
  public void unAssignRolesFromUser(RevokeRoleFromUserCommand command) {
    if (command == null
        || command.userId() == null
        || command.roleIds() == null
        || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }
    userRoleWriteRepository.deleteByUserIdAndRoleIdIn(command.userId(), command.roleIds());
  }
}
