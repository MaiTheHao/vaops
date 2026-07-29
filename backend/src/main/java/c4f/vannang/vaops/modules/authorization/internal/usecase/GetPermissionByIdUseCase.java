package c4f.vannang.vaops.modules.authorization.internal.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.GetPermissionByIdQuery;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPermissionByIdUseCase {

  private final PermissionQueryRepository permissionQueryRepository;

  public PermissionResponse execute(GetPermissionByIdQuery query) {
    if (query == null || query.permissionId() == null) {
      throw new ValidationException("GetPermissionByIdQuery and permissionId must not be null");
    }

    Permission permission = permissionQueryRepository
        .findActiveById(query.permissionId())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + query.permissionId()));

    return mapToPermissionResponse(permission);
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
