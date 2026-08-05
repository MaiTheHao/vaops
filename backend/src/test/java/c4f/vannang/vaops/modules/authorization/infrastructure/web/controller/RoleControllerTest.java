package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignPermissionsRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.RoleWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignPermissionsToRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreateRoleCommand;
import c4f.vannang.vaops.modules.authorization.internal.service.RoleService;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private RoleService roleService;

  @MockitoBean
  private AuthorizationWebMapper mapper;

  @MockitoBean
  private AccessTokenSpec accessTokenSpec;

  @MockitoBean
  private IdentityUserAPIService identityUserService;

  private final UUID roleId = UUID.randomUUID();

  @Test
  @DisplayName("POST /api/v1/roles with valid body returns 201 and created role")
  void createRole_validBody_returnsCreated() throws Exception {
    CreateRoleWebRequestDto request = new CreateRoleWebRequestDto("ADMIN", "Administrator role");
    CreateRoleCommand command = new CreateRoleCommand("ADMIN", "Administrator role", null);
    Role role = Role.create(new RoleCode("ADMIN"), "Administrator role");
    RoleWebResponseDto response = new RoleWebResponseDto(
        roleId, "ADMIN", "Administrator role", true, null, Instant.now(), Instant.now());

    when(mapper.toCreateRoleCommand(any())).thenReturn(command);
    when(roleService.createRole(command)).thenReturn(role);
    when(mapper.toRoleWebResponseDto(role)).thenReturn(response);

    mockMvc.perform(post("/api/v1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("ADMIN"))
        .andExpect(jsonPath("$.description").value("Administrator role"));
  }

  @Test
  @DisplayName("POST /api/v1/roles with blank code returns 400")
  void createRole_blankCode_returnsBadRequest() throws Exception {
    CreateRoleWebRequestDto request = new CreateRoleWebRequestDto("", "Administrator role");

    mockMvc.perform(post("/api/v1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/roles/{id} returns 200")
  void getRole_returnsOk() throws Exception {
    Role role = Role.create(new RoleCode("ADMIN"), "Administrator role");
    RoleWebResponseDto response = new RoleWebResponseDto(
        roleId, "ADMIN", "Administrator role", true, null, Instant.now(), Instant.now());

    when(roleService.getRoleById(roleId)).thenReturn(role);
    when(mapper.toRoleWebResponseDto(role)).thenReturn(response);

    mockMvc.perform(get("/api/v1/roles/{id}", roleId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("ADMIN"));
  }

  @Test
  @DisplayName("DELETE /api/v1/roles/{id} soft deletes and returns 204")
  void deleteRole_ShouldExecuteCorrectDeleteTypeAndReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/roles/{id}", roleId))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(roleService).softDeleteRole(eq(roleId), isNull());
    verify(roleService, never()).hardDeleteRole(any());
  }

  @Test
  @DisplayName("POST /api/v1/roles/{id}/permissions returns 204")
  void assignPermissions_returnsNoContent() throws Exception {
    Set<UUID> permissionIds = Set.of(UUID.randomUUID());
    AssignPermissionsRequestDto request = new AssignPermissionsRequestDto(permissionIds);
    AssignPermissionsToRoleCommand command = new AssignPermissionsToRoleCommand(roleId, permissionIds);

    when(mapper.toAssignPermissionsToRoleCommand(eq(roleId), any())).thenReturn(command);

    mockMvc.perform(post("/api/v1/roles/{id}/permissions", roleId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(roleService).assignPermissionsToRole(command);
  }
}