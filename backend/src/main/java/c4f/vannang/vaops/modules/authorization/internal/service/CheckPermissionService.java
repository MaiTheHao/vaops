package c4f.vannang.vaops.modules.authorization.internal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.dto.CheckPermissionQuery;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckPermissionService {

  private final PermissionQueryRepository permissionQueryRepository;

  public boolean checkPermission(CheckPermissionQuery query) {
    if (query == null || query.userId() == null || query.resource() == null || query.action() == null) {
      throw new ValidationException("UserId, resource, and action must not be null");
    }

    PermissionResource resource = new PermissionResource(query.resource());
    PermissionAction action = new PermissionAction(query.action());

    return permissionQueryRepository.hasPermission(query.userId(), resource.value(), action.value());
  }
}
