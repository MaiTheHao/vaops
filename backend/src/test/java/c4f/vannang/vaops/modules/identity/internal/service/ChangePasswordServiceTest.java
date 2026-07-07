package c4f.vannang.vaops.modules.identity.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  private ChangePasswordService changePasswordService;

  @BeforeEach
  void setUp() {
    changePasswordService = new ChangePasswordService(userQueryRepository, userWriteRepository, passwordEncoder);
  }

  @Test
  void execute_shouldChangePassword() {
    UUID userId = UUID.randomUUID();
    User user = User.register("testuser", "old-hash", "Test", "avatar");
    user.setId(userId);
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
    when(passwordEncoder.encode("new-password-123")).thenReturn("new-hash");
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    changePasswordService.execute(userId, "old-password", "new-password-123");

    assertEquals("new-hash", user.getPasswordHash());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> changePasswordService.execute(userId, "old", "new-password-123"));
  }

  @Test
  void execute_shouldThrowWhenOldPasswordInvalid() {
    UUID userId = UUID.randomUUID();
    User user = User.register("testuser", "old-hash", "Test", "avatar");
    user.setId(userId);
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

    assertThrows(BusinessRuleViolationException.class, () -> changePasswordService.execute(userId, "wrong-password", "new-password-123"));
  }

  @Test
  void execute_shouldThrowForShortNewPassword() {
    UUID userId = UUID.randomUUID();
    assertThrows(ValidationException.class, () -> changePasswordService.execute(userId, "old", "short"));
  }
}
