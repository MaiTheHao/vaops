package c4f.vannang.vaops.modules.authorization.internal.service;

import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRolesToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;

public interface UserRoleService {
  void assignRolesToUser(AssignRolesToUserCommand command);
  void unAssignRolesFromUser(RevokeRoleFromUserCommand command);
}
