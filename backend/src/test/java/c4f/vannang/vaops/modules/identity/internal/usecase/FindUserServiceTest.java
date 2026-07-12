package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.api.dto.FindByAccountNameQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
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
class FindUserServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  private FindUserService findUserService;

  @BeforeEach
  void setUp() {
    findUserService = new FindUserService(userQueryRepository);
  }

  @Test
  void findById_shouldReturnUser() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.of(user));

    Optional<User> result = findUserService.findById(new FindByIdQuery(userId));

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getAccountName().value());
  }

  @Test
  void findById_shouldReturnEmptyWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findActiveById(userId)).thenReturn(Optional.empty());

    Optional<User> result = findUserService.findById(new FindByIdQuery(userId));
    assertTrue(result.isEmpty());
  }

  @Test
  void findByAccountName_shouldReturnUser() {
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    when(userQueryRepository.findActiveByAccountName(new AccountName("testuser")))
        .thenReturn(Optional.of(user));

    Optional<User> result =
        findUserService.findByAccountName(new FindByAccountNameQuery("testuser"));

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getAccountName().value());
  }

  @Test
  void findByAccountName_shouldReturnEmptyWhenNotFound() {
    when(userQueryRepository.findActiveByAccountName(new AccountName("nonexistent")))
        .thenReturn(Optional.empty());

    Optional<User> result =
        findUserService.findByAccountName(new FindByAccountNameQuery("nonexistent"));
    assertTrue(result.isEmpty());
  }

  @Test
  void findByAccountName_shouldReturnEmptyForNullInput() {
    Optional<User> result = findUserService.findByAccountName(new FindByAccountNameQuery(null));
    assertTrue(result.isEmpty());
    verifyNoInteractions(userQueryRepository);
  }

  @Test
  void findForAuth_shouldReturnUser() {
    UUID userId = UUID.randomUUID();
    User user = User.register(
        new AccountName("testuser"),
        new PasswordHash("hashed-password"),
        new DisplayName("Test"),
        new AvatarUrl("avatar"));
    user.setId(userId);
    when(userQueryRepository.findActiveByAccountName(new AccountName("testuser")))
        .thenReturn(Optional.of(user));

    Optional<User> result = findUserService.findForAuth(new FindForAuthQuery("testuser"));

    assertTrue(result.isPresent());
    assertEquals(userId, result.get().getId());
    assertEquals("hashed-password", result.get().getPasswordHash().value());
  }

  @Test
  void findForAuth_shouldReturnEmptyForNullInput() {
    Optional<User> result = findUserService.findForAuth(new FindForAuthQuery(null));
    assertTrue(result.isEmpty());
    verifyNoInteractions(userQueryRepository);
  }
}
