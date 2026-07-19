package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
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
class UpdateProfileUseCaseTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  @Mock
  private UserWriteRepository userWriteRepository;

  private UpdateProfileUseCase UpdateProfileUseCase;

  @BeforeEach
  void setUp() {
    UpdateProfileUseCase = new UpdateProfileUseCase(userQueryRepository, userWriteRepository);
  }

  @Test
  void execute_shouldUpdateProfile() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Old Name"),
        new AvatarUrl("old-avatar"));
    user.setId(userId);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    UpdateProfileUseCase.execute(new UpdateProfileCommand(userId, "New Name", "new-avatar"));

    assertEquals("New Name", user.getDisplayName().value());
    assertEquals("new-avatar", user.getAvatarUrl().value());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> UpdateProfileUseCase.execute(new UpdateProfileCommand(userId, "Name", "avatar")));
  }
}
