package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UserWebResponseDto;
import c4f.vannang.vaops.modules.identity.infrastructure.web.mapper.IdentityWebMapper;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private IdentityWebMapper mapper;

  @MockitoBean
  private AccessTokenSpec accessTokenSpec;

  @MockitoBean
  private IdentityUserAPIService identityUserService;

  private UUID userId;
  private User user;
  private UserWebResponseDto userDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.register(new AccountName("john_doe"), new PasswordHash("encoded"), null, null);
    userDto = new UserWebResponseDto(
        userId, "john_doe", null, null, true, null, null, null);
  }

  @Test
  @DisplayName("GET /api/v1/users/{id} returns 200 with user details")
  void getUser_returnsOk() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.of(user));
    when(mapper.toUserWebResponseDto(user)).thenReturn(userDto);

    mockMvc.perform(get("/api/v1/users/{id}", userId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.accountName").value("john_doe"));
  }

  @Test
  @DisplayName("GET /api/v1/users/{id} returns 404 when user not found")
  void getUser_notFound_returns404() throws Exception {
    when(userService.findUserById(any())).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/users/{id}", userId))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /api/v1/users/{id} soft deletes and returns 204")
  void deleteUser_soft_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/users/{id}", userId))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(userService).softDeleteUser(eq(userId), any());
  }

  @Test
  @DisplayName("DELETE /api/v1/users/{id}?hard=true hard deletes and returns 204")
  void deleteUser_hard_returnsNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/users/{id}", userId).param("hard", "true"))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(userService).hardDeleteUser(userId);
  }

  @Test
  @DisplayName("GET /api/v1/users returns paged results")
  void searchUsers_returnsOk() throws Exception {
    when(userService.searchUsers(any())).thenReturn(
        new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1));
    when(mapper.toUserPageResponse(any())).thenReturn(
        new PageResponse<>(List.of(userDto), 0, 20, 1L, 1, false, false));

    mockMvc.perform(get("/api/v1/users")
            .param("page", "0")
            .param("size", "20"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].accountName").value("john_doe"))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20));
  }

  @Test
  @DisplayName("PATCH /api/v1/users/{id}/status toggles status and returns 204")
  void toggleUserStatus_returnsNoContent() throws Exception {
    mockMvc.perform(patch("/api/v1/users/{id}/status", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"active\": false}"))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(userService).toggleStatus(new ToggleUserStatusCommand(userId, false));
  }
}
