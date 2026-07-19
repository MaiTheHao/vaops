package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindUserByAccountNameUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  private FindUserByAccountNameUseCase findUserByAccountNameUseCase;

  @BeforeEach
  void setUp() {
    findUserByAccountNameUseCase = new FindUserByAccountNameUseCase(userQueryRepository);
  }

  @Test
  void execute_shouldReturnUser() {
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    when(userQueryRepository.findActiveByAccountName(new AccountName("testuser")))
        .thenReturn(Optional.of(user));

    Optional<User> result =
        findUserByAccountNameUseCase.execute(new FindByAccountNameCommand("testuser"));

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getAccountName().value());
  }

  @Test
  void execute_shouldReturnEmptyWhenNotFound() {
    when(userQueryRepository.findActiveByAccountName(new AccountName("nonexistent")))
        .thenReturn(Optional.empty());

    Optional<User> result =
        findUserByAccountNameUseCase.execute(new FindByAccountNameCommand("nonexistent"));

    assertTrue(result.isEmpty());
  }

  @Test
  void execute_shouldReturnEmptyForNullInput() {
    Optional<User> result = findUserByAccountNameUseCase.execute(new FindByAccountNameCommand(null));

    assertTrue(result.isEmpty());
    verifyNoInteractions(userQueryRepository);
  }
}
