package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckAvailableUserUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  @InjectMocks
  private CheckAvailableUserUseCase checkAvailableUserUseCase;

  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
  }

  @Test
  void execute_shouldPass_whenUserIsActiveAndNotLocked() {
    User user = mock(User.class);
    when(user.isActive()).thenReturn(true);
    when(user.isLocked()).thenReturn(false);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));

    assertDoesNotThrow(() -> checkAvailableUserUseCase.execute(new CheckAvailableUserCommand(userId)));
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_whenUserNotFound() {
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.empty());

    assertThrows(
        UnauthenticatedException.class,
        () -> checkAvailableUserUseCase.execute(new CheckAvailableUserCommand(userId)));
  }

  @Test
  void execute_shouldThrowUnauthenticatedException_whenUserInactive() {
    User user = mock(User.class);
    when(user.isActive()).thenReturn(false);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        UnauthenticatedException.class,
        () -> checkAvailableUserUseCase.execute(new CheckAvailableUserCommand(userId)));
  }

  @Test
  void execute_shouldThrowAccountLockedException_whenUserLocked() {
    User user = mock(User.class);
    when(user.isActive()).thenReturn(true);
    when(user.isLocked()).thenReturn(true);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));

    assertThrows(
        AccountLockedException.class,
        () -> checkAvailableUserUseCase.execute(new CheckAvailableUserCommand(userId)));
  }
}
