package c4f.vannang.vaops.modules.authorization.internal.mapper;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;

@Component
public class PermissionResponseMapper {

  public PermissionResponse toResponse(Permission permission) {
    if (permission == null) return null;
    return PermissionResponse.builder()
        .id(permission.getId())
        .resource(permission.getResource() != null ? permission.getResource().value() : null)
        .action(permission.getAction() != null ? permission.getAction().value() : null)
        .description(permission.getDescription() != null ? permission.getDescription().value() : null)
        .isActive(permission.isActive())
        .createdAt(permission.getCreatedAt())
        .updatedAt(permission.getUpdatedAt())
        .deletedAt(permission.getDeletedAt())
        .deletedBy(permission.getDeletedBy())
        .createdBy(permission.getCreatedBy())
        .updatedBy(permission.getUpdatedBy())
        .build();
  }
}
