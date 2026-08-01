package c4f.vannang.vaops.modules.authorization.internal.service;

import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.shared.dto.PageResponse;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
  PermissionResponse createPermission(CreatePermissionCommand command);
  PermissionResponse updatePermission(UpdatePermissionCommand command);
  void softDeletePermission(UUID id, UUID deletedBy);
  void hardDeletePermission(UUID id);
  PermissionResponse getPermissionById(UUID id);
  PageResponse<PermissionResponse> searchPermissions(PermissionSearchCriteria criteria);
  boolean hasPermission(UUID userId, String resource, String action);
  List<PermissionResponse> getPermissionsByUserId(UUID userId);
}
