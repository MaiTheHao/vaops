package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserProfileService userProfileService;

  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.register(
        new AccountName("john_doe"),
        new PasswordHash("encoded_pass"),
        new DisplayName("John Doe"),
        new AvatarUrl("http://avatar.com/john.png")
    );
  }

  @Test
  @org.junit.jupiter.api.DisplayName("getProfile should return user when user exists")
  void getProfile_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userProfileService.getProfile(new FindByIdCommand(userId));

    assertThat(result).isNotNull();
    assertThat(result.getAccountName().value()).isEqualTo("john_doe");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("getProfile should throw ResourceNotFoundException when user does not exist")
  void getProfile_NotFound() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userProfileService.getProfile(new FindByIdCommand(userId)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("updateProfile should update display name and avatar url")
  void updateProfile_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User updated = userProfileService.updateProfile(new UpdateProfileCommand(userId, "Jane Doe", "http://avatar.com/jane.png"));

    assertThat(updated.getDisplayName().value()).isEqualTo("Jane Doe");
    assertThat(updated.getAvatarUrl().value()).isEqualTo("http://avatar.com/jane.png");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("changePassword should update password hash when old password matches")
  void changePassword_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("OldPass123!", "encoded_pass")).thenReturn(true);
    when(passwordEncoder.encode("NewPass123!")).thenReturn("new_encoded_pass");

    userProfileService.changePassword(new ChangePasswordCommand(userId, "OldPass123!", "NewPass123!"));

    verify(userWriteRepository).save(user);
    assertThat(user.getPasswordHash().value()).isEqualTo("new_encoded_pass");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("changePassword should throw exception when old password does not match")
  void changePassword_InvalidOldPassword() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("WrongPass!", "encoded_pass")).thenReturn(false);

    assertThatThrownBy(() -> userProfileService.changePassword(new ChangePasswordCommand(userId, "WrongPass!", "NewPass123!")))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessage("Invalid old password");
  }
}
