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
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ToggleUserStatusServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private UserWriteRepository userWriteRepository;

  private ToggleUserStatusService toggleUserStatusService;

  @BeforeEach
  void setUp() {
    toggleUserStatusService = new ToggleUserStatusService(userQueryRepository, userWriteRepository);
  }

  @Test
  void execute_shouldActivateUser() {
    UUID userId = UUID.randomUUID();
    User user = User.register("testuser", "hashed", "Test", "avatar");
    user.setId(userId);
    user.deactivate();
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    toggleUserStatusService.execute(userId, true);

    assertTrue(user.isActive());
    verify(userWriteRepository).save(user);
  }

  @Test
  void execute_shouldDeactivateUser() {
    UUID userId = UUID.randomUUID();
    User user = User.register("testuser", "hashed", "Test", "avatar");
    user.setId(userId);
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
    when(userWriteRepository.save(any(User.class))).thenReturn(user);

    toggleUserStatusService.execute(userId, false);

    assertFalse(user.isActive());
  }

  @Test
  void execute_shouldThrowWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> toggleUserStatusService.execute(userId, true));
  }
}
