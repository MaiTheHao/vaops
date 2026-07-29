package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateRoleUseCase {

  private final RoleQueryRepository roleQueryRepository;
  private final RoleWriteRepository roleWriteRepository;
  private final PermissionQueryRepository permissionQueryRepository;

  public RoleResponse execute(UpdateRoleCommand command) {
    if (command == null || command.id() == null || command.code() == null || command.code().isBlank()) {
      throw new ValidationException("Role id and code must not be empty");
    }

    Role role = roleQueryRepository
        .findActiveById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found or is inactive: " + command.id()));

    String newCode = command.code().trim();
    if (!role.getCode().equalsIgnoreCase(newCode)) {
      Optional<Role> existingWithCode = roleQueryRepository.findByCode(newCode);
      if (existingWithCode.isPresent() && !existingWithCode.get().getId().equals(role.getId())) {
        throw new ResourceAlreadyExistsException("Role with code '" + newCode + "' already exists");
      }
      role.setCode(newCode);
    }

    role.setDescription(command.description());
    role.setUpdatedBy(command.updatedBy());

    if (command.permissionIds() != null) {
      if (command.permissionIds().isEmpty()) {
        role.setPermissions(new HashSet<>());
      } else {
        List<Permission> permissions = permissionQueryRepository.findAllActiveByIds(new ArrayList<>(command.permissionIds()));
        role.setPermissions(new HashSet<>(permissions));
      }
    }

    Role savedRole = roleWriteRepository.save(role);
    return mapToRoleResponse(savedRole);
  }

  private RoleResponse mapToRoleResponse(Role role) {
    Set<PermissionResponse> permissionResponses = role.getPermissions() != null
        ? role.getPermissions().stream()
            .filter(p -> p.getIsActive() && p.getDeletedAt() == null)
            .map(this::mapToPermissionResponse)
            .collect(Collectors.toSet())
        : Set.of();

    return new RoleResponse(
        role.getId(),
        role.getCode(),
        role.getDescription(),
        role.getIsActive(),
        role.getCreatedAt(),
        role.getUpdatedAt(),
        permissionResponses
    );
  }

  private PermissionResponse mapToPermissionResponse(Permission p) {
    return new PermissionResponse(
        p.getId(),
        p.getResource(),
        p.getAction(),
        p.getDescription(),
        p.getIsActive(),
        p.getCreatedAt(),
        p.getUpdatedAt()
    );
  }
}
