package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.PermissionWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.service.PermissionService;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private PermissionService permissionService;

  @MockitoBean
  private AuthorizationWebMapper mapper;

  @MockitoBean
  private AccessTokenSpec accessTokenSpec;

  @MockitoBean
  private IdentityUserAPIService identityUserService;

  private final UUID permissionId = UUID.randomUUID();

  @Test
  @DisplayName("POST /api/v1/permissions with valid body returns 201 and created permission")
  void createPermission_validBody_returnsCreated() throws Exception {
    CreatePermissionWebRequestDto request = new CreatePermissionWebRequestDto("USER", "READ", "Read user");
    CreatePermissionCommand command = new CreatePermissionCommand("USER", "READ", "Read user");
    Permission permission = Permission.create(
        new PermissionResource("USER"),
        new PermissionAction("READ"),
        new PermissionDescription("Read user"));
    PermissionWebResponseDto response = new PermissionWebResponseDto(
        permissionId, "USER:READ", "USER", "READ", "Read user", true, Instant.now(), Instant.now());

    when(mapper.toCreatePermissionCommand(any())).thenReturn(command);
    when(permissionService.createPermission(command)).thenReturn(permission);
    when(mapper.toPermissionWebResponseDto(permission)).thenReturn(response);

    mockMvc.perform(post("/api/v1/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("USER:READ"))
        .andExpect(jsonPath("$.resource").value("USER"))
        .andExpect(jsonPath("$.action").value("READ"));
  }

  @Test
  @DisplayName("POST /api/v1/permissions with blank resource returns 400")
  void createPermission_blankResource_returnsBadRequest() throws Exception {
    CreatePermissionWebRequestDto request = new CreatePermissionWebRequestDto("", "READ", "Read user");

    mockMvc.perform(post("/api/v1/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/v1/permissions/{id} returns 200")
  void getPermission_returnsOk() throws Exception {
    Permission permission = Permission.create(
        new PermissionResource("USER"),
        new PermissionAction("READ"),
        new PermissionDescription("Read user"));
    PermissionWebResponseDto response = new PermissionWebResponseDto(
        permissionId, "USER:READ", "USER", "READ", "Read user", true, Instant.now(), Instant.now());

    when(permissionService.getPermissionById(permissionId)).thenReturn(permission);
    when(mapper.toPermissionWebResponseDto(permission)).thenReturn(response);

    mockMvc.perform(get("/api/v1/permissions/{id}", permissionId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("USER:READ"));
  }

  @Test
  @DisplayName("DELETE /api/v1/permissions/{id} soft deletes and returns 204")
  void deletePermission_ShouldExecuteCorrectDeleteTypeAndReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/permissions/{id}", permissionId))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(permissionService).softDeletePermission(eq(permissionId), isNull());
    verify(permissionService, never()).hardDeletePermission(any());
  }
}