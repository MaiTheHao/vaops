package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UpdateProfileWebRequest;
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
import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserByIdUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
import java.security.Principal;
import java.util.Optional;
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
    private FindUserByIdUseCase findUserByIdUseCase;
    @Mock
    private UpdateProfileUseCase updateProfileUseCase;
    @Mock
    private ChangePasswordUseCase changePasswordUseCase;
    @Mock
    private SoftDeleteUseCase softDeleteUseCase;

    @InjectMocks
    private ProfileController controller;

    private UUID userId;
    private Principal principal;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        principal = mock(Principal.class);
        lenient().when(principal.getName()).thenReturn(userId.toString());

        user = User.register(
            new AccountName("test.user"),
            new PasswordHash("hashed-pw"),
            new DisplayName("Test User"),
            new AvatarUrl("http://avatar.url")
        );
        user.setId(userId);
    }

    @Test
    void getMyProfile_ShouldReturnProfile() {
        when(findUserByIdUseCase.execute(any(FindByIdCommand.class))).thenReturn(Optional.of(user));

        ResponseEntity<ProfileWebResponse> response = controller.getMyProfile(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().id());
        assertEquals("test.user", response.getBody().accountName());
        assertEquals("Test User", response.getBody().displayName());
        assertEquals("http://avatar.url", response.getBody().avatarUrl());
        verify(findUserByIdUseCase).execute(new FindByIdCommand(userId));
    }

    @Test
    void updateProfile_ShouldExecuteUpdateAndReturnUpdatedProfile() {
        UpdateProfileWebRequest webRequest = new UpdateProfileWebRequest("New Name", "http://new.avatar");

        user.updateProfile(new DisplayName("New Name"), new AvatarUrl("http://new.avatar"));
        when(findUserByIdUseCase.execute(any(FindByIdCommand.class))).thenReturn(Optional.of(user));

        ResponseEntity<ProfileWebResponse> response = controller.updateProfile(webRequest, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Name", response.getBody().displayName());
        assertEquals("http://new.avatar", response.getBody().avatarUrl());
        verify(updateProfileUseCase).execute(new UpdateProfileCommand(userId, "New Name", "http://new.avatar"));
    }

    @Test
    void changePassword_ShouldExecuteChangeAndReturnOk() {
        ChangePasswordWebRequest webRequest = new ChangePasswordWebRequest("old-password", "new-password");

        ResponseEntity<Void> response = controller.changePassword(webRequest, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(changePasswordUseCase).execute(new ChangePasswordCommand(userId, "old-password", "new-password"));
    }

    @Test
    void deleteAccount_ShouldExecuteSoftDeleteAndReturnNoContent() {
        ResponseEntity<Void> response = controller.deleteAccount(principal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(softDeleteUseCase).execute(new SoftDeleteUserCommand(userId, userId));
    }
}
