package c4f.vannang.vaops.modules.authorization.api.service;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import java.util.List;
import java.util.UUID;

public interface AuthorizationAPIService {
  List<RoleDto> getRolesByUserId(UUID userId);
  List<PermissionDto> getPermissionsByUserId(UUID userId);
}