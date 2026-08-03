package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityProfileAPIService;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.PutUpdateProfileWebRequest;
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
  private IdentityProfileAPIService identityProfileService;

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
    // given
    when(identityProfileService.getProfile(new FindByIdQuery(userId))).thenReturn(user);

    // when
    ResponseEntity<ProfileWebResponse> response = profileController.getMyProfile(principal);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(userId);
    assertThat(response.getBody().accountName()).isEqualTo("test.user");
    assertThat(response.getBody().displayName()).isEqualTo("Test User");
    assertThat(response.getBody().avatarUrl()).isEqualTo("https://example.com/avatar.png");

    verify(identityProfileService).getProfile(new FindByIdQuery(userId));
  }

  @Test
  void getMyProfile_ShouldHandleNullValueObjects_WhenUserHasNullFields() {
    // given
    UserDto userWithNulls = new UserDto(userId, null, null, null, true, null, null, null);
    when(identityProfileService.getProfile(new FindByIdQuery(userId))).thenReturn(userWithNulls);

    // when
    ResponseEntity<ProfileWebResponse> response = profileController.getMyProfile(principal);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(userId);
    assertThat(response.getBody().accountName()).isNull();
    assertThat(response.getBody().displayName()).isNull();
    assertThat(response.getBody().avatarUrl()).isNull();
  }

  @Test
  void getMyProfile_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
    // given
    when(identityProfileService.getProfile(new FindByIdQuery(userId)))
        .thenThrow(new ResourceNotFoundException("User not found"));

    // when / then
    assertThatThrownBy(() -> profileController.getMyProfile(principal))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void putUpdateProfile_ShouldExecuteUpdateAndReturnUpdatedProfile_WhenRequestValid() {
    // given
    PutUpdateProfileWebRequest request =
        new PutUpdateProfileWebRequest("New Display Name", "https://example.com/new-avatar.png");

    UserDto updated = new UserDto(
        userId, "test.user", "New Display Name", "https://example.com/new-avatar.png",
        true, null, null, null);

    when(identityProfileService.updateProfile(
            new UpdateProfileRequest(userId, "New Display Name", "https://example.com/new-avatar.png")))
        .thenReturn(updated);

    // when
    ResponseEntity<ProfileWebResponse> response = profileController.putUpdateProfile(request, principal);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().displayName()).isEqualTo("New Display Name");
    assertThat(response.getBody().avatarUrl()).isEqualTo("https://example.com/new-avatar.png");

    verify(identityProfileService).updateProfile(
        new UpdateProfileRequest(userId, "New Display Name", "https://example.com/new-avatar.png"));
  }

  @Test
  void changePassword_ShouldExecuteChangeAndReturnOk_WhenRequestValid() {
    // given
    ChangePasswordWebRequest request = new ChangePasswordWebRequest("OldPass123!", "NewPass123!");

    // when
    ResponseEntity<Void> response = profileController.changePassword(request, principal);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNull();

    verify(identityProfileService).changePassword(
        new ChangePasswordRequest(userId, "OldPass123!", "NewPass123!"));
  }

  @Test
  void deleteAccount_ShouldExecuteSoftDeleteAndReturnNoContent_WhenHardIsFalse() {
    // when
    ResponseEntity<Void> response = profileController.deleteAccount(false, principal);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();

    verify(userService).softDeleteUser(userId, userId);
  }

  @Test
  void deleteAccount_ShouldExecuteHardDeleteAndReturnNoContent_WhenHardIsTrue() {
    // when
    ResponseEntity<Void> response = profileController.deleteAccount(true, principal);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();

    verify(userService).hardDeleteUser(userId);
  }
}