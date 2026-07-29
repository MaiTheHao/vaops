package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateRoleUseCase {

  private final RoleQueryRepository roleQueryRepository;
  private final RoleWriteRepository roleWriteRepository;
  private final PermissionQueryRepository permissionQueryRepository;

  public RoleResponse execute(CreateRoleCommand command) {
    if (command == null || command.code() == null || command.code().isBlank()) {
      throw new ValidationException("Role code must not be empty");
    }

    String code = command.code().trim().toUpperCase();
    if (roleQueryRepository.findByCode(code).isPresent()) {
      throw new ResourceAlreadyExistsException("Role with code '" + code + "' already exists");
    }

    Set<Permission> permissions = new HashSet<>();
    if (command.permissionIds() != null && !command.permissionIds().isEmpty()) {
      List<Permission> activePermissions = permissionQueryRepository.findAllActiveByIds(new ArrayList<>(command.permissionIds()));
      permissions.addAll(activePermissions);
    }

    Role role = Role.builder()
        .code(code)
        .description(command.description())
        .permissions(permissions)
        .createdBy(command.createdBy())
        .isActive(true)
        .build();

    Role savedRole = roleWriteRepository.save(role);
    return mapToRoleResponse(savedRole);
  }

  private RoleResponse mapToRoleResponse(Role role) {
    Set<PermissionResponse> permissionResponses = role.getPermissions() != null
        ? role.getPermissions().stream()
            .filter(p -> p.getIsActive() && p.getDeletedAt() == null)
            .map(this::mapToPermissionResponse)
            .collect(java.util.stream.Collectors.toSet())
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
