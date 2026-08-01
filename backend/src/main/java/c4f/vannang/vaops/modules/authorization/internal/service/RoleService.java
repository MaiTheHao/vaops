package c4f.vannang.vaops.modules.authorization.internal.service;

import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionsToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokePermissionFromRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdateRoleCommand;
import c4f.vannang.vaops.shared.dto.PageResponse;
import java.util.List;
import java.util.UUID;

public interface RoleService {
  RoleResponse createRole(CreateRoleCommand command);
  RoleResponse updateRole(UpdateRoleCommand command);
  void softDeleteRole(UUID id, UUID deletedBy);
  void hardDeleteRole(UUID id);
  RoleResponse getRoleById(UUID id);
  List<RoleResponse> getRolesByUserId(UUID userId);
  PageResponse<RoleResponse> searchRoles(RoleSearchCriteria criteria);
  void assignPermissionsToRole(AssignPermissionsToRoleCommand command);
  void unassignPermissionsFromRole(RevokePermissionFromRoleCommand command);
}
