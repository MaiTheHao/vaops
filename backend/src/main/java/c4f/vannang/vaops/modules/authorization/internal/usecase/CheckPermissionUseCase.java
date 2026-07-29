package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.CheckPermissionQuery;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckPermissionUseCase {

  private final PermissionQueryRepository permissionQueryRepository;

  public boolean execute(CheckPermissionQuery query) {
    if (query == null) {
      throw new ValidationException("CheckPermissionQuery must not be null");
    }
    return execute(query.userId(), query.resource(), query.action());
  }

  public boolean execute(UUID userId, String resource, String action) {
    if (userId == null || resource == null || action == null) {
      throw new ValidationException("UserId, resource, and action must not be null");
    }

    List<Permission> permissions = permissionQueryRepository.findActivePermissionsByUserId(userId);
    return permissions.stream()
        .anyMatch(p -> p.getResource().equalsIgnoreCase(resource.trim())
                    && p.getAction().equalsIgnoreCase(action.trim()));
  }
}
