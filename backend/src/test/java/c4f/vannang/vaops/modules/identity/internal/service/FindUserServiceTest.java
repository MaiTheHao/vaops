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

import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.mapper.UserDtoMapper;

@ExtendWith(MockitoExtension.class)
class FindUserServiceTest {

  @Mock
  private UserQueryRepository userQueryRepository;

  private final UserDtoMapper userDtoMapper = new UserDtoMapper();
  private FindUserService findUserService;

  @BeforeEach
  void setUp() {
    findUserService = new FindUserService(userQueryRepository, userDtoMapper);
  }

  @Test
  void findById_shouldReturnUserDto() {
    UUID userId = UUID.randomUUID();
    User user = User.register("testuser", "hashed", "Test", "avatar");
    user.setId(userId);
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

    Optional<UserDto> result = findUserService.findById(userId);

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().accountName());
  }

  @Test
  void findById_shouldReturnEmptyWhenUserNotFound() {
    UUID userId = UUID.randomUUID();
    when(userQueryRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

    Optional<UserDto> result = findUserService.findById(userId);

    assertTrue(result.isEmpty());
  }

  @Test
  void findByAccountName_shouldReturnUserDto() {
    User user = User.register("testuser", "hashed", "Test", "avatar");
    when(userQueryRepository.findByAccountNameAndDeletedAtIsNull("testuser")).thenReturn(Optional.of(user));

    Optional<UserDto> result = findUserService.findByAccountName("testuser");

    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().accountName());
  }

  @Test
  void findByAccountName_shouldReturnEmptyWhenNotFound() {
    when(userQueryRepository.findByAccountNameAndDeletedAtIsNull("nonexistent")).thenReturn(Optional.empty());

    Optional<UserDto> result = findUserService.findByAccountName("nonexistent");

    assertTrue(result.isEmpty());
  }

  @Test
  void findByAccountName_shouldReturnEmptyForNullInput() {
    Optional<UserDto> result = findUserService.findByAccountName(null);

    assertTrue(result.isEmpty());
    verifyNoInteractions(userQueryRepository);
  }

  @Test
  void findForAuth_shouldReturnUserAuthDto() {
    UUID userId = UUID.randomUUID();
    User user = User.register("testuser", "hashed-password", "Test", "avatar");
    user.setId(userId);
    when(userQueryRepository.findByAccountNameAndDeletedAtIsNull("testuser")).thenReturn(Optional.of(user));

    Optional<UserAuthDto> result = findUserService.findForAuth("testuser");

    assertTrue(result.isPresent());
    assertEquals(userId, result.get().id());
    assertEquals("hashed-password", result.get().passwordHash());
  }

  @Test
  void findForAuth_shouldReturnEmptyForNullInput() {
    Optional<UserAuthDto> result = findUserService.findForAuth(null);

    assertTrue(result.isEmpty());
    verifyNoInteractions(userQueryRepository);
  }
}
