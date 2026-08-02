package c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignPermissionsRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignRolesToUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokePermissionsRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokeRoleFromUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.UpdatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.UpdateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.PermissionWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.RoleWebResponseDto;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.RolePermission;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionsToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRolesToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokePermissionFromRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdateRoleCommand;
import c4f.vannang.vaops.shared.dto.PageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthorizationWebMapper {

    CreateRoleCommand toCreateRoleCommand(CreateRoleWebRequestDto dto);

    UpdateRoleCommand toUpdateRoleCommand(UUID id, UpdateRoleWebRequestDto dto);

    AssignPermissionsToRoleCommand toAssignPermissionsToRoleCommand(UUID roleId, AssignPermissionsRequestDto dto);

    RevokePermissionFromRoleCommand toRevokePermissionFromRoleCommand(UUID roleId, RevokePermissionsRequestDto dto);

    CreatePermissionCommand toCreatePermissionCommand(CreatePermissionWebRequestDto dto);

    UpdatePermissionCommand toUpdatePermissionCommand(UUID id, UpdatePermissionWebRequestDto dto);

    AssignRolesToUserCommand toAssignRolesToUserCommand(UUID userId, AssignRolesToUserWebRequestDto dto);

    RevokeRoleFromUserCommand toRevokeRoleFromUserCommand(UUID userId, RevokeRoleFromUserWebRequestDto dto);

    @Mapping(target = "code", expression = "java(role.getCode().value())")
    @Mapping(target = "permissions", expression = "java(toPermissionWebResponseDtos(role.getRolePermissions()))")
    RoleWebResponseDto toRoleWebResponseDto(Role role);

    @Mapping(target = "code", expression = "java(c4f.vannang.vaops.modules.authorization.api.util.PermissionUtils.format(permission.getResource().value(), permission.getAction().value()))")
    @Mapping(target = "resource", expression = "java(permission.getResource().value())")
    @Mapping(target = "action", expression = "java(permission.getAction().value())")
    @Mapping(target = "description", expression = "java(permission.getDescription() == null ? null : permission.getDescription().value())")
    PermissionWebResponseDto toPermissionWebResponseDto(Permission permission);

    default PageResponse<RoleWebResponseDto> toRolePageResponse(PageResponse<Role> page) {
        List<RoleWebResponseDto> content = page.content().stream().map(this::toRoleWebResponseDto).toList();
        return new PageResponse<>(content, page.page(), page.size(), page.totalElements(), page.totalPages(), page.hasNext(), page.hasPrevious());
    }

    default PageResponse<PermissionWebResponseDto> toPermissionPageResponse(PageResponse<Permission> page) {
        List<PermissionWebResponseDto> content = page.content().stream().map(this::toPermissionWebResponseDto).toList();
        return new PageResponse<>(content, page.page(), page.size(), page.totalElements(), page.totalPages(), page.hasNext(), page.hasPrevious());
    }

    default Set<PermissionWebResponseDto> toPermissionWebResponseDtos(Set<RolePermission> rolePermissions) {
        if (rolePermissions == null) {
            return null;
        }
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .map(this::toPermissionWebResponseDto)
                .collect(Collectors.toSet());
    }
}