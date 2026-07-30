package c4f.vannang.vaops.modules.authorization.internal.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleResponseMapper {

  private final PermissionResponseMapper permissionResponseMapper;

  public RoleResponse toResponse(Role role) {
    if (role == null) return null;
    
    Set<PermissionResponse> permissionResponses = role.getRolePermissions() == null
        ? Collections.emptySet()
        : role.getRolePermissions().stream()
            .map(rp -> permissionResponseMapper.toResponse(rp.getPermission()))
            .collect(Collectors.toSet());

    return RoleResponse.builder()
        .id(role.getId())
        .code(role.getCode() != null ? role.getCode().value() : null)
        .description(role.getDescription())
        .isActive(role.isActive())
        .permissions(permissionResponses)
        .createdAt(role.getCreatedAt())
        .updatedAt(role.getUpdatedAt())
        .deletedAt(role.getDeletedAt())
        .deletedBy(role.getDeletedBy())
        .createdBy(role.getCreatedBy())
        .updatedBy(role.getUpdatedBy())
        .build();
  }
}
