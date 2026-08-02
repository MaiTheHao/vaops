package c4f.vannang.vaops.modules.authorization.internal.service;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.shared.dto.PageResponse;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
  Permission createPermission(CreatePermissionCommand command);
  Permission updatePermission(UpdatePermissionCommand command);
  void softDeletePermission(UUID id, UUID deletedBy);
  void hardDeletePermission(UUID id);
  Permission getPermissionById(UUID id);
  PageResponse<Permission> searchPermissions(PermissionSearchCriteria criteria);
  boolean hasPermission(UUID userId, String resource, String action);
  List<Permission> getPermissionsByUserId(UUID userId);
}
