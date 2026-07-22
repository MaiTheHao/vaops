package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindUserByIdUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  private FindUserByIdUseCase findUserByIdUseCase;

  @BeforeEach
  void setUp() {
    findUserByIdUseCase = new FindUserByIdUseCase(userQueryRepository);
  }

  @Test
  void execute_shouldReturnUser() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    Optional<User> result = findUserByIdUseCase.execute(new FindByIdCommand(userId));

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getAccountName().value());
  }

  @Test
  void execute_shouldReturnEmptyWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    Optional<User> result = findUserByIdUseCase.execute(new FindByIdCommand(userId));

    assertTrue(result.isEmpty());
  }
}
