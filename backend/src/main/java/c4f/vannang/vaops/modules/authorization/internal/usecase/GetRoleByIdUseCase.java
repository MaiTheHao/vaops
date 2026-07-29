package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.GetRoleByIdQuery;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetRoleByIdUseCase {

  private final RoleQueryRepository roleQueryRepository;

  public RoleResponse execute(GetRoleByIdQuery query) {
    if (query == null || query.roleId() == null) {
      throw new ValidationException("GetRoleByIdQuery and roleId must not be null");
    }

    Role role = roleQueryRepository
        .findActiveById(query.roleId())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + query.roleId()));

    return mapToRoleResponse(role);
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
