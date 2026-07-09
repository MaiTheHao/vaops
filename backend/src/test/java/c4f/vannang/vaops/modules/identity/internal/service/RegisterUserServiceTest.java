package c4f.vannang.vaops.modules.identity.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.modules.identity.internal.mapper.UserDtoMapper;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

  @Mock
  private UserWriteRepository userWriteRepository;
  @Mock
  private UserQueryRepository userQueryRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  private final UserDtoMapper userDtoMapper = new UserDtoMapper();
  private RegisterUserService registerUserService;

  @BeforeEach
  void setUp() {
    registerUserService = new RegisterUserService(userQueryRepository, userWriteRepository, passwordEncoder, userDtoMapper);
  }

  @Test
  void execute_shouldRegisterUserSuccessfully() {
    RegisterDto dto = new RegisterDto("testuser", "password123", "Test User", "https://example.com/avatar.png");
    when(userQueryRepository.existsActiveByAccountName(any(AccountName.class))).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
    when(userWriteRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserDto result = registerUserService.execute(dto);

    assertNotNull(result);
    assertEquals("testuser", result.accountName());
    assertEquals("Test User", result.displayName());
  }

  @Test
  void execute_shouldThrowValidationExceptionForShortPassword() {
    RegisterDto dto = new RegisterDto("testuser", "short", "Test", "avatar");

    assertThrows(ValidationException.class, () -> registerUserService.execute(dto));
  }

  @Test
  void execute_shouldThrowValidationExceptionForBlankAccountName() {
    RegisterDto dto = new RegisterDto("   ", "password123", "Test", "avatar");

    assertThrows(ValidationException.class, () -> registerUserService.execute(dto));
  }

  @Test
  void execute_shouldThrowValidationExceptionForNullAccountName() {
    RegisterDto dto = new RegisterDto(null, "password123", "Test", "avatar");

    assertThrows(ValidationException.class, () -> registerUserService.execute(dto));
  }

  @Test
  void execute_shouldThrowValidationExceptionForTooLongAccountName() {
    String longName = "a".repeat(257);
    RegisterDto dto = new RegisterDto(longName, "password123", "Test", "avatar");

    assertThrows(ValidationException.class, () -> registerUserService.execute(dto));
  }

  @Test
  void execute_shouldThrowResourceAlreadyExistsException() {
    RegisterDto dto = new RegisterDto("testuser", "password123", "Test", "avatar");
    when(userQueryRepository.existsActiveByAccountName(any(AccountName.class))).thenReturn(true);

    assertThrows(ResourceAlreadyExistsException.class, () -> registerUserService.execute(dto));
  }
}