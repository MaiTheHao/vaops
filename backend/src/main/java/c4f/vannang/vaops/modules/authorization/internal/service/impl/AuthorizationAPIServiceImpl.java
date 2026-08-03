package c4f.vannang.vaops.modules.authorization.internal.service.impl;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import c4f.vannang.vaops.modules.authorization.api.mapper.AuthorizationApiMapper;
import c4f.vannang.vaops.modules.authorization.api.service.AuthorizationAPIService;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRolesToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AuthorizationAPIServiceImpl implements AuthorizationAPIService {

  private final RoleQueryRepository roleQueryRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final AuthorizationApiMapper authorizationApiMapper;
  private final AuthProperties authProperties;
  private final UserRoleService userRoleService;

  @Override
  public List<RoleDto> getRolesByUserId(UUID userId) {
    List<Role> roles = roleQueryRepository.findAllActiveByUserId(userId);
    return authorizationApiMapper.toRoleDtoList(roles);
  }

  @Override
  public List<PermissionDto> getPermissionsByUserId(UUID userId) {
    List<Permission> permissions = permissionQueryRepository.findAllActiveByUserId(userId);
    return authorizationApiMapper.toPermissionDtoList(permissions);
  }

  @Override
  @Transactional
  public void assignDefaultRoleToUser(UUID userId) {
    Role defaultRole = roleQueryRepository
        .findActiveByCode(new RoleCode(authProperties.getDefaultRoleCode()))
        .orElseThrow(() -> new ResourceNotFoundException(
            "Default role not found: " + authProperties.getDefaultRoleCode()));
    userRoleService.assignRolesToUser(
        new AssignRolesToUserCommand(userId, Set.of(defaultRole.getId()), null));
  }
}
