package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.ListRolesQuery;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesUseCase {

  private final RoleQueryRepository roleQueryRepository;

  public List<RoleResponse> execute(ListRolesQuery query) {
    List<Role> roles = roleQueryRepository.findAllActive();
    return roles.stream()
        .map(this::mapToRoleResponse)
        .collect(Collectors.toList());
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
