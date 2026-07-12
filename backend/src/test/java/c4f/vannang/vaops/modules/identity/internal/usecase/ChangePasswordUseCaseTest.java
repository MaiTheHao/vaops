package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  @Mock
  private UserWriteRepository userWriteRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private ChangePasswordUseCase ChangePasswordUseCase;

  @BeforeEach
  void setUp() {
    ChangePasswordUseCase =
        new ChangePasswordUseCase(userQueryRepository, userWriteRepository, passwordEncoder);
  }

  @Test
  void execute_shouldChangePassword() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("old-hash"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
    when(passwordEncoder.encode("new-password-123")).thenReturn("new-hash");
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    ChangePasswordUseCase.execute(
        new ChangePasswordCommand(userId, "old-password", "new-password-123"));

    assertEquals("new-hash", user.getPasswordHash().value());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> ChangePasswordUseCase.execute(
            new ChangePasswordCommand(userId, "old", "new-password-123")));
  }

  @Test
  void execute_shouldThrowWhenOldPasswordInvalid() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("old-hash"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

    assertThrows(
        BusinessRuleViolationException.class,
        () -> ChangePasswordUseCase.execute(
            new ChangePasswordCommand(userId, "wrong-password", "new-password-123")));
  }

  @Test
  void execute_shouldThrowForShortNewPassword() {
    UUID userId = UUID.randomUUID();
    assertThrows(
        ValidationException.class,
        () -> ChangePasswordUseCase.execute(new ChangePasswordCommand(userId, "old", "short")));
  }
}
