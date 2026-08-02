package c4f.vannang.vaops.modules.identity.internal.service.impl;

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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserProfileServiceImpl userProfileService;

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
  void getProfile_ShouldReturnUser_WhenUserExists() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    User result = userProfileService.getProfile(new FindByIdCommand(userId));

    // then
    assertThat(result).isNotNull();
    assertThat(result.getAccountName().value()).isEqualTo("john_doe");
    assertThat(result.getDisplayName().value()).isEqualTo("John Doe");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("getProfile should throw ResourceNotFoundException when user does not exist")
  void getProfile_ShouldThrowResourceNotFound_WhenUserDoesNotExist() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> userProfileService.getProfile(new FindByIdCommand(userId)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("updateProfile should update display name and avatar url")
  void updateProfile_ShouldUpdateDisplayNameAndAvatarUrl_WhenUserExists() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(argThat(u -> u == user))).thenAnswer(inv -> inv.getArgument(0));

    // when
    User updated = userProfileService.updateProfile(
        new UpdateProfileCommand(userId, "Jane Doe", "http://avatar.com/jane.png"));

    // then
    assertThat(updated.getDisplayName().value()).isEqualTo("Jane Doe");
    assertThat(updated.getAvatarUrl().value()).isEqualTo("http://avatar.com/jane.png");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("changePassword should update password hash when old password matches")
  void changePassword_ShouldUpdatePasswordHash_WhenOldPasswordMatches() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("OldPass123!", "encoded_pass")).thenReturn(true);
    when(passwordEncoder.encode("NewPass123!")).thenReturn("new_encoded_pass");

    // when
    userProfileService.changePassword(new ChangePasswordCommand(userId, "OldPass123!", "NewPass123!"));

    // then
    verify(userWriteRepository).save(user);
    assertThat(user.getPasswordHash().value()).isEqualTo("new_encoded_pass");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("changePassword should throw BusinessRuleViolationException when old password is invalid")
  void changePassword_ShouldThrowBusinessRuleViolation_WhenOldPasswordIsInvalid() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("WrongPass!", "encoded_pass")).thenReturn(false);

    // when / then
    assertThatThrownBy(() -> userProfileService.changePassword(
        new ChangePasswordCommand(userId, "WrongPass!", "NewPass123!")))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessage("Invalid old password");
  }
}