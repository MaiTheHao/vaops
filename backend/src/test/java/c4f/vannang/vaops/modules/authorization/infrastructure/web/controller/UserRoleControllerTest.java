package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignRolesToUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokeRoleFromUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.dto.AssignRolesToUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.RevokeRoleFromUserCommand;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRoleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private UserRoleService userRoleService;

  @MockitoBean
  private AuthorizationWebMapper mapper;

  @MockitoBean
  private AccessTokenSpec accessTokenSpec;

  @MockitoBean
  private IdentityUserAPIService identityUserService;

  private final UUID userId = UUID.randomUUID();
  private final Set<UUID> roleIds = Set.of(UUID.randomUUID());

  @Test
  @DisplayName("POST /api/v1/users/{userId}/roles returns 204")
  void assignRoles_returnsNoContent() throws Exception {
    AssignRolesToUserWebRequestDto request = new AssignRolesToUserWebRequestDto(roleIds);
    AssignRolesToUserCommand command = new AssignRolesToUserCommand(userId, roleIds, null);

    when(mapper.toAssignRolesToUserCommand(eq(userId), any())).thenReturn(command);

    mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(userRoleService).assignRolesToUser(command);
  }

  @Test
  @DisplayName("DELETE /api/v1/users/{userId}/roles returns 204")
  void revokeRoles_returnsNoContent() throws Exception {
    RevokeRoleFromUserWebRequestDto request = new RevokeRoleFromUserWebRequestDto(roleIds);
    RevokeRoleFromUserCommand command = new RevokeRoleFromUserCommand(userId, roleIds, null);

    when(mapper.toRevokeRoleFromUserCommand(eq(userId), any())).thenReturn(command);

    mockMvc.perform(delete("/api/v1/users/{userId}/roles", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(userRoleService).unAssignRolesFromUser(command);
  }
}