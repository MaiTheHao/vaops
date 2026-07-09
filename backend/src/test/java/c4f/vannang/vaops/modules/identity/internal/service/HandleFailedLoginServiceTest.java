package c4f.vannang.vaops.modules.identity.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class HandleFailedLoginServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;

  private HandleFailedLoginService handleFailedLoginService;

  @BeforeEach
  void setUp() {
    handleFailedLoginService = new HandleFailedLoginService(userQueryRepository, userWriteRepository);
  }

  @Test
  void execute_shouldIncrementFailedLoginCount() {
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar")
    );
    when(userQueryRepository.findActiveByAccountName(new AccountName("testuser"))).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    handleFailedLoginService.execute("testuser");

    assertEquals(1, user.getFailedLoginCount());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldLockAccountAfterMaxAttempts() {
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar")
    );
    for (int i = 0; i < 4; i++) {
      user.recordFailedLogin(5, Duration.ofMinutes(15));
    }
    when(userQueryRepository.findActiveByAccountName(new AccountName("testuser"))).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    handleFailedLoginService.execute("testuser");

    assertEquals(5, user.getFailedLoginCount());
    assertNotNull(user.getLockedUntil());
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    when(userQueryRepository.findActiveByAccountName(new AccountName("unknown"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> handleFailedLoginService.execute("unknown"));
  }
}