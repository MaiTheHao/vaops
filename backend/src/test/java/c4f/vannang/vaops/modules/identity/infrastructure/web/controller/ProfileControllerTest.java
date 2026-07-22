package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.PutUpdateProfileWebRequest;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.GetProfileUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
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
  private GetProfileUseCase getProfileUseCase;

  @Mock
  private UpdateProfileUseCase updateProfileUseCase;

  @Mock
  private ChangePasswordUseCase changePasswordUseCase;

  @Mock
  private SoftDeleteUseCase softDeleteUseCase;

  @InjectMocks
  private ProfileController profileController;

  private UUID userId;
  private AuthenticatedPrincipal principal;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    principal = new AuthenticatedPrincipal(userId, "test.user");

    user = User.register(
        new AccountName("test.user"),
        new PasswordHash("hashed-password"),
        new DisplayName("Test User"),
        new AvatarUrl("https://example.com/avatar.png")
    );
    user.setId(userId);
  }

  @Test
  void getMyProfile_ShouldReturnProfile_WhenUserExists() {
    when(getProfileUseCase.execute(any(FindByIdCommand.class))).thenReturn(user);

    ResponseEntity<ProfileWebResponse> response = profileController.getMyProfile(principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(userId, response.getBody().id());
    assertEquals("test.user", response.getBody().accountName());
    assertEquals("Test User", response.getBody().displayName());
    assertEquals("https://example.com/avatar.png", response.getBody().avatarUrl());

    verify(getProfileUseCase).execute(new FindByIdCommand(userId));
  }

  @Test
  void getMyProfile_ShouldHandleNullValueObjects_InResponse() {
    User userWithNulls = User.register(null, new PasswordHash("hashed"), null, null);
    userWithNulls.setId(userId);

    when(getProfileUseCase.execute(any(FindByIdCommand.class))).thenReturn(userWithNulls);

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
    when(getProfileUseCase.execute(any(FindByIdCommand.class)))
        .thenThrow(new ResourceNotFoundException("User not found"));

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileController.getMyProfile(principal)
    );
  }

  @Test
  void putUpdateProfile_ShouldExecuteUpdateAndReturnUpdatedProfile() {
    PutUpdateProfileWebRequest request = new PutUpdateProfileWebRequest("New Display Name", "https://example.com/new-avatar.png");
    user.updateProfile(new DisplayName("New Display Name"), new AvatarUrl("https://example.com/new-avatar.png"));

    when(updateProfileUseCase.execute(any(UpdateProfileCommand.class))).thenReturn(user);

    ResponseEntity<ProfileWebResponse> response = profileController.putUpdateProfile(request, principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("New Display Name", response.getBody().displayName());
    assertEquals("https://example.com/new-avatar.png", response.getBody().avatarUrl());

    verify(updateProfileUseCase).execute(new UpdateProfileCommand(userId, "New Display Name", "https://example.com/new-avatar.png"));
  }

  @Test
  void changePassword_ShouldExecuteChangeAndReturnOk() {
    ChangePasswordWebRequest request = new ChangePasswordWebRequest("OldPass123!", "NewPass123!");

    ResponseEntity<Void> response = profileController.changePassword(request, principal);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());

    verify(changePasswordUseCase).execute(new ChangePasswordCommand(userId, "OldPass123!", "NewPass123!"));
  }

  @Test
  void deleteAccount_ShouldExecuteSoftDeleteAndReturnNoContent() {
    ResponseEntity<Void> response = profileController.deleteAccount(principal);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    assertNull(response.getBody());

    verify(softDeleteUseCase).execute(new SoftDeleteUserCommand(userId, userId));
  }
}
