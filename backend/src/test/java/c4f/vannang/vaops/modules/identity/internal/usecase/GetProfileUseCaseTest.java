package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  private GetProfileUseCase getProfileUseCase;

  @BeforeEach
  void setUp() {
    getProfileUseCase = new GetProfileUseCase(userQueryRepository);
  }

  @Test
  void execute_shouldReturnUserWhenFound() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test User"),
        new AvatarUrl("avatar"));
    user.setId(userId);

    when(userQueryRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = getProfileUseCase.execute(new FindByIdCommand(userId));

    assertNotNull(result);
    assertEquals(userId, result.getId());
    verify(userQueryRepository).findById(userId);
  }

  @Test
  void execute_shouldThrowExceptionWhenNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> getProfileUseCase.execute(new FindByIdCommand(userId)));
  }
}
