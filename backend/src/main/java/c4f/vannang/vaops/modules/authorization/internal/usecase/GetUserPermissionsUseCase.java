package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserPermissionsUseCase {

  private final PermissionQueryRepository permissionQueryRepository;

  public List<PermissionResponse> execute(UUID userId) {
    if (userId == null) {
      throw new ValidationException("UserId must not be null");
    }

    List<Permission> permissions = permissionQueryRepository.findActivePermissionsByUserId(userId);
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
