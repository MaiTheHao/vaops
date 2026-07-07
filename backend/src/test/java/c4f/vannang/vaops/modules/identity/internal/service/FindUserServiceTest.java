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
}
