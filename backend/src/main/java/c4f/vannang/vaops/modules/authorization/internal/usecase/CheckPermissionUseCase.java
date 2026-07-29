package c4f.vannang.vaops.modules.authorization.internal.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    if (query == null
        || query.userId() == null
        || query.resource() == null
        || query.resource().isBlank()
        || query.action() == null
        || query.action().isBlank()) {
      throw new ValidationException(
          "CheckPermissionQuery, userId, resource, and action must not be null or empty");
    }

    return permissionQueryRepository.hasPermission(
        query.userId(),
        query.resource().trim().toUpperCase(),
        query.action().trim().toUpperCase()
    );
  }
}
