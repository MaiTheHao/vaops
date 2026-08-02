package c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.PermissionWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.RoleWebResponseDto;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.shared.dto.PageResponse;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthorizationWebMapperTest {

  private final AuthorizationWebMapper mapper = Mappers.getMapper(AuthorizationWebMapper.class);

  @Test
  @DisplayName("Should map CreateRoleWebRequestDto to CreateRoleCommand")
  void toCreateRoleCommand_mapsCodeAndDescription() {
    CreateRoleWebRequestDto dto = new CreateRoleWebRequestDto("ADMIN", "Administrator role");

    CreateRoleCommand command = mapper.toCreateRoleCommand(dto);

    assertEquals("ADMIN", command.code());
    assertEquals("Administrator role", command.description());
  }

  @Test
  @DisplayName("Should map Permission to PermissionWebResponseDto with formatted code")
  void toPermissionWebResponseDto_mapsFormattedCode() {
    Permission permission = Permission.create(
        new PermissionResource("USER"),
        new PermissionAction("READ"),
        null);

    PermissionWebResponseDto dto = mapper.toPermissionWebResponseDto(permission);

    assertEquals("USER:READ", dto.code());
    assertEquals("USER", dto.resource());
    assertEquals("READ", dto.action());
  }

  @Test
  @DisplayName("Should map PageResponse<Role> to PageResponse<RoleWebResponseDto> preserving pagination")
  void toRolePageResponse_preservesPaginationMetadata() {
    Role role = Role.create(new RoleCode("ADMIN"), "Administrator role");
    PageResponse<Role> page = new PageResponse<>(
        java.util.List.of(role), 0, 10, 1L, 1, false, false);

    PageResponse<RoleWebResponseDto> result = mapper.toRolePageResponse(page);

    assertEquals(1, result.content().size());
    assertEquals(1L, result.totalElements());
    assertEquals(0, result.page());
    assertEquals(10, result.size());
    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
    assertEquals("ADMIN", result.content().get(0).code());
  }
}