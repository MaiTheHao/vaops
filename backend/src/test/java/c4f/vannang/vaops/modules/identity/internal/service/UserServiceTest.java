package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.*;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

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
  @org.junit.jupiter.api.DisplayName("register should save new user when account name is available")
  void register_Success() {
    RegisterCommand cmd = new RegisterCommand("john_doe", "Pass123!", "John", null);
    when(userQueryRepository.existsByAccountName(any(AccountName.class))).thenReturn(false);
    when(passwordEncoder.encode("Pass123!")).thenReturn("encoded_pass");
    when(userWriteRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User registered = userService.register(cmd);

    assertThat(registered).isNotNull();
    assertThat(registered.getAccountName().value()).isEqualTo("john_doe");
  }

  @Test
  @org.junit.jupiter.api.DisplayName("register should throw ResourceAlreadyExistsException when account name exists")
  void register_AlreadyExists() {
    RegisterCommand cmd = new RegisterCommand("john_doe", "Pass123!", "John", null);
    when(userQueryRepository.existsByAccountName(any(AccountName.class))).thenReturn(true);

    assertThatThrownBy(() -> userService.register(cmd))
        .isInstanceOf(ResourceAlreadyExistsException.class);
  }

  @Test
  @org.junit.jupiter.api.DisplayName("checkAvailableUser should succeed when user is active and unlocked")
  void checkAvailableUser_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.checkAvailableUser(new CheckAvailableUserCommand(userId));
  }

  @Test
  @org.junit.jupiter.api.DisplayName("checkAvailableUser should throw UnauthenticatedException when user not found")
  void checkAvailableUser_NotFound() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.checkAvailableUser(new CheckAvailableUserCommand(userId)))
        .isInstanceOf(UnauthenticatedException.class);
  }

  @Test
  @org.junit.jupiter.api.DisplayName("softDelete should soft delete user")
  void softDelete_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.softDelete(new SoftDeleteUserCommand(userId, userId));

    verify(userWriteRepository).save(user);
    assertThat(user.getDeletedAt()).isNotNull();
  }

  @Test
  @org.junit.jupiter.api.DisplayName("toggleStatus should activate/deactivate user")
  void toggleStatus_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.toggleStatus(new ToggleUserStatusCommand(userId, false));
    assertThat(user.isActive()).isFalse();

    userService.toggleStatus(new ToggleUserStatusCommand(userId, true));
    assertThat(user.isActive()).isTrue();
  }

  @Test
  @org.junit.jupiter.api.DisplayName("recordSuccessfulLogin should reset login failures")
  void recordSuccessfulLogin_Success() {
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.recordSuccessfulLogin(new RecordSuccessfulLoginCommand(userId));

    verify(userWriteRepository).save(user);
  }

  @Test
  @org.junit.jupiter.api.DisplayName("recordFailedLogin should record failed attempt")
  void recordFailedLogin_Success() {
    when(userQueryRepository.findByAccountName(any(AccountName.class))).thenReturn(Optional.of(user));

    userService.recordFailedLogin(new RecordFailedLoginCommand("john_doe"));

    verify(userWriteRepository).save(user);
  }
}
