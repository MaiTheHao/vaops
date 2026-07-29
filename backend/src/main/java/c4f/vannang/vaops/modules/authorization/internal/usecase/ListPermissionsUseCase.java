package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.ListPermissionsQuery;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListPermissionsUseCase {

  private final PermissionQueryRepository permissionQueryRepository;

  public List<PermissionResponse> execute(ListPermissionsQuery query) {
    List<Permission> permissions = permissionQueryRepository.findAllActive();
    return permissions.stream()
        .map(this::mapToPermissionResponse)
        .collect(Collectors.toList());
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
