package c4f.vannang.vaops.modules.identity.internal.service.impl;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
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
class UserServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.register(
        new AccountName("john_doe"),
        new PasswordHash("encoded_pass"),
        null,
        null
    );
  }

  @Test
  void register_ShouldSaveNewUser_WhenAccountNameIsAvailable() {
    // given
    RegisterCommand cmd = new RegisterCommand("john_doe", "Pass123!", "John", null);
    when(userQueryRepository.existsByAccountName(new AccountName("john_doe"))).thenReturn(false);
    when(passwordEncoder.encode("Pass123!")).thenReturn("encoded_pass");
    when(userWriteRepository.save(argThat(u -> u != null))).thenAnswer(inv -> inv.getArgument(0));

    // when
    User registered = userService.register(cmd);

    // then
    assertThat(registered).isNotNull();
    assertThat(registered.getAccountName().value()).isEqualTo("john_doe");
  }

  @Test
  void register_ShouldThrowResourceAlreadyExistsException_WhenAccountNameAlreadyExists() {
    // given
    RegisterCommand cmd = new RegisterCommand("john_doe", "Pass123!", "John", null);
    when(userQueryRepository.existsByAccountName(new AccountName("john_doe"))).thenReturn(true);

    // when
    assertThatThrownBy(() -> userService.register(cmd))
        // then
        .isInstanceOf(ResourceAlreadyExistsException.class)
        .hasMessage("Account name already exists");
  }

  @Test
  void checkAvailableUser_ShouldSucceed_WhenUserIsActiveAndUnlocked() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    userService.checkAvailableUser(new CheckAvailableUserCommand(userId));

    // then
    assertThat(user.isActive()).isTrue();
    assertThat(user.isLocked()).isFalse();
  }

  @Test
  void checkAvailableUser_ShouldThrowUnauthenticatedException_WhenUserNotFound() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    // when
    assertThatThrownBy(() -> userService.checkAvailableUser(new CheckAvailableUserCommand(userId)))
        // then
        .isInstanceOf(UnauthenticatedException.class)
        .hasMessage("Invalid credentials");
  }

  @Test
  void softDelete_ShouldSoftDeleteUser_WhenUserExists() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    userService.softDelete(new SoftDeleteUserCommand(userId, userId));

    // then
    verify(userWriteRepository).save(user);
    assertThat(user.isDeleted()).isTrue();
  }

  @Test
  void softDeleteUser_ShouldSoftDeleteUser_WhenUserExists() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    userService.softDeleteUser(userId, userId);

    // then
    verify(userWriteRepository).save(user);
    assertThat(user.isDeleted()).isTrue();
  }

  @Test
  void softDeleteUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    // when / then
    assertThatThrownBy(() -> userService.softDeleteUser(userId, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void hardDeleteUser_ShouldDeleteUser_WhenUserExists() {
    // given
    when(userQueryRepository.existsByIdWithDeleted(userId)).thenReturn(true);

    // when
    userService.hardDeleteUser(userId);

    // then
    verify(userWriteRepository).deleteById(userId);
  }

  @Test
  void hardDeleteUser_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
    // given
    when(userQueryRepository.existsByIdWithDeleted(userId)).thenReturn(false);

    // when / then
    assertThatThrownBy(() -> userService.hardDeleteUser(userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found");
  }

  @Test
  void toggleStatus_ShouldActivateUser_WhenActiveFlagIsTrue() {
    // given
    user.deactivate();
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    userService.toggleStatus(new ToggleUserStatusCommand(userId, true));

    // then
    verify(userWriteRepository).save(user);
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void toggleStatus_ShouldDeactivateUser_WhenActiveFlagIsFalse() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    userService.toggleStatus(new ToggleUserStatusCommand(userId, false));

    // then
    verify(userWriteRepository).save(user);
    assertThat(user.isActive()).isFalse();
  }

  @Test
  void recordSuccessfulLogin_ShouldResetLoginFailures_WhenUserExists() {
    // given
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    // when
    userService.recordSuccessfulLogin(new RecordSuccessfulLoginCommand(userId));

    // then
    verify(userWriteRepository).save(user);
  }

  @Test
  void recordFailedLogin_ShouldRecordFailedAttempt_WhenUserExists() {
    // given
    when(userQueryRepository.findByAccountName(new AccountName("john_doe")))
        .thenReturn(Optional.of(user));

    // when
    userService.recordFailedLogin(new RecordFailedLoginCommand("john_doe"));

    // then
    verify(userWriteRepository).save(user);
  }
}