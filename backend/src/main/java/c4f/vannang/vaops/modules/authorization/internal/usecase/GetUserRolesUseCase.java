package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserRolesUseCase {

  private final RoleQueryRepository roleQueryRepository;

  public List<RoleResponse> execute(UUID userId) {
    if (userId == null) {
      throw new ValidationException("UserId must not be null");
    }

    List<Role> roles = roleQueryRepository.findActiveRolesByUserId(userId);
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
