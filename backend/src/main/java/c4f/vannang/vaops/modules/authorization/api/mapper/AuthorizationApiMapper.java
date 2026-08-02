package c4f.vannang.vaops.modules.authorization.api.mapper;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorizationApiMapper {

  @Mapping(target = "code", expression = "java(role.getCode() != null ? role.getCode().value() : null)")
  @Mapping(target = "description", expression = "java(role.getDescription())")
  RoleDto toRoleDto(Role role);

  List<RoleDto> toRoleDtoList(List<Role> roles);

  @Mapping(target = "resource", expression = "java(permission.getResource() != null ? permission.getResource().value() : null)")
  @Mapping(target = "action", expression = "java(permission.getAction() != null ? permission.getAction().value() : null)")
  @Mapping(target = "description", expression = "java(permission.getDescription() != null ? permission.getDescription().value() : null)")
  PermissionDto toPermissionDto(Permission permission);

  List<PermissionDto> toPermissionDtoList(List<Permission> permissions);
}