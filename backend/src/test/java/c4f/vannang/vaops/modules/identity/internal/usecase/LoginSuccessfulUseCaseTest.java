package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginSuccessfulUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  @Mock
  private UserWriteRepository userWriteRepository;

  private LoginSuccessfulUseCase LoginSuccessfulUseCase;

  @BeforeEach
  void setUp() {
    LoginSuccessfulUseCase = new LoginSuccessfulUseCase(userQueryRepository, userWriteRepository);
  }

  @Test
  void execute_shouldResetFailedLoginCount() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    user.recordFailedLogin(5, java.time.Duration.ofMinutes(15));
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    LoginSuccessfulUseCase.execute(new RecordSuccessfulLoginCommand(userId));

    assertEquals(0, user.getFailedLoginCount());
    assertNotNull(user.getLastLoginAt());
    assertNull(user.getLockedUntil());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> LoginSuccessfulUseCase.execute(new RecordSuccessfulLoginCommand(userId)));
  }
}
