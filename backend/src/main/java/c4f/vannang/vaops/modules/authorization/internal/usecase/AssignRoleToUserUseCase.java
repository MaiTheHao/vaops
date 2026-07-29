package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRoleToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.UserRoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignRoleToUserUseCase {

  private final RoleQueryRepository roleQueryRepository;
  private final UserRoleQueryRepository userRoleQueryRepository;
  private final UserRoleWriteRepository userRoleWriteRepository;

  public void execute(AssignRoleToUserCommand command) {
    if (command == null || command.userId() == null || command.roleIds() == null || command.roleIds().isEmpty()) {
      throw new ValidationException("UserId and roleIds must not be empty");
    }

    List<UUID> roleIdList = new ArrayList<>(command.roleIds());
    List<Role> activeRoles = roleQueryRepository.findAllActiveByIds(roleIdList);

    if (activeRoles.size() != command.roleIds().size()) {
      throw new ResourceNotFoundException("One or more roles were not found or are inactive");
    }

    List<UserRole> toSave = new ArrayList<>();
    for (UUID roleId : command.roleIds()) {
      UserRoleId userRoleId = new UserRoleId(command.userId(), roleId);
      Optional<UserRole> existingOpt = userRoleQueryRepository.findById(userRoleId);

      if (existingOpt.isPresent()) {
        UserRole ur = existingOpt.get();
        ur.setRevokedAt(null);
        ur.setRevokedBy(null);
        ur.setAssignedAt(Instant.now());
        ur.setAssignedBy(command.assignedBy());
        toSave.add(ur);
      } else {
        UserRole ur = UserRole.builder()
            .id(userRoleId)
            .assignedAt(Instant.now())
            .assignedBy(command.assignedBy())
            .build();
        toSave.add(ur);
      }
    }

    userRoleWriteRepository.saveAll(toSave);
  }
}
