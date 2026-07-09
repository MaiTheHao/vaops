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

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class SoftDeleteUserServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;

  private SoftDeleteUserService softDeleteUserService;

  @BeforeEach
  void setUp() {
    softDeleteUserService = new SoftDeleteUserService(userQueryRepository, userWriteRepository);
  }

  @Test
  void execute_shouldSoftDeleteUser() {
    UUID userId = UUID.randomUUID();
    UUID deletedBy = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar")
    );
    user.setId(userId);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    softDeleteUserService.execute(userId, deletedBy);

    assertNotNull(user.getDeletedAt());
    assertEquals(deletedBy, user.getDeletedBy());
    assertFalse(user.isActive());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> softDeleteUserService.execute(userId, userId));
  }
}
