package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.dto.FindForAuthCommand;
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
class FindUserForAuthUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  private FindUserForAuthUseCase findUserForAuthUseCase;

  @BeforeEach
  void setUp() {
    findUserForAuthUseCase = new FindUserForAuthUseCase(userQueryRepository);
  }

  @Test
  void execute_shouldReturnUser() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed-password"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    when(userQueryRepository.findActiveByAccountName(new AccountName("testuser")))
        .thenReturn(Optional.of(user));

    Optional<User> result = findUserForAuthUseCase.execute(new FindForAuthCommand("testuser"));

    assertTrue(result.isPresent());
    assertEquals(userId, result.get().getId());
    assertEquals("hashed-password", result.get().getPasswordHash().value());
  }

  @Test
  void execute_shouldReturnEmptyForNullInput() {
    Optional<User> result = findUserForAuthUseCase.execute(new FindForAuthCommand(null));

    assertTrue(result.isEmpty());
    verifyNoInteractions(userQueryRepository);
  }
}
