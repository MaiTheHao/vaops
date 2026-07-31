package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityProfileService;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.PutUpdateProfileWebRequest;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

  @Mock
  private IdentityProfileService identityProfileService;

  @Mock
  private UserService userService;

  @InjectMocks
  private ProfileController profileController;

  private UUID userId;
  private AuthenticatedPrincipal principal;
  private UserDto user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    principal = new AuthenticatedPrincipal(userId, "test.user");

    user = new UserDto(
        userId,
        "test.user",
        "Test User",
        "https://example.com/avatar.png",
        true,
        null,
        null,
        null);
  }

  @Test
  void getMyProfile_ShouldReturnProfile_WhenUserExists() {
    when(identityProfileService.getProfile(any(FindByIdQuery.class))).thenReturn(user);

    ResponseEntity<ProfileWebResponse> response = profileController.getMyProfile(principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().id());
    assertEquals("test.user", response.getBody().accountName());
    assertEquals("Test User", response.getBody().displayName());
    assertEquals("https://example.com/avatar.png", response.getBody().avatarUrl());

    verify(identityProfileService).getProfile(new FindByIdQuery(userId));
  }

  @Test
  void getMyProfile_ShouldHandleNullValueObjects_InResponse() {
    UserDto userWithNulls = new UserDto(userId, null, null, null, true, null, null, null);

    when(identityProfileService.getProfile(any(FindByIdQuery.class))).thenReturn(userWithNulls);

    ResponseEntity<ProfileWebResponse> response = profileController.getMyProfile(principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().id());
    assertNull(response.getBody().accountName());
    assertNull(response.getBody().displayName());
    assertNull(response.getBody().avatarUrl());
  }

  @Test
  void getMyProfile_ShouldThrowException_WhenUserNotFound() {
    when(identityProfileService.getProfile(any(FindByIdQuery.class)))
        .thenThrow(new ResourceNotFoundException("User not found"));

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileController.getMyProfile(principal)
    );
  }

  @Test
  void putUpdateProfile_ShouldExecuteUpdateAndReturnUpdatedProfile() {
    PutUpdateProfileWebRequest request = new PutUpdateProfileWebRequest("New Display Name", "https://example.com/new-avatar.png");

    UserDto updated = new UserDto(
        userId, "test.user", "New Display Name", "https://example.com/new-avatar.png",
        true, null, null, null);

    when(identityProfileService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(updated);

    ResponseEntity<ProfileWebResponse> response = profileController.putUpdateProfile(request, principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("New Display Name", response.getBody().displayName());
    assertEquals("https://example.com/new-avatar.png", response.getBody().avatarUrl());

    verify(identityProfileService).updateProfile(new UpdateProfileRequest(userId, "New Display Name", "https://example.com/new-avatar.png"));
  }

  @Test
  void changePassword_ShouldExecuteChangeAndReturnOk() {
    ChangePasswordWebRequest request = new ChangePasswordWebRequest("OldPass123!", "NewPass123!");

    ResponseEntity<Void> response = profileController.changePassword(request, principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());

    verify(identityProfileService).changePassword(new ChangePasswordRequest(userId, "OldPass123!", "NewPass123!"));
  }

  @Test
  void deleteAccount_ShouldExecuteSoftDeleteAndReturnNoContent() {
    ResponseEntity<Void> response = profileController.deleteAccount(principal);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());

    verify(userService).softDelete(new SoftDeleteUserCommand(userId, userId));
  }
}
