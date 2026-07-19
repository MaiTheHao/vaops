package c4f.vannang.vaops.modules.identity.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

  @Mock
  private UserWriteRepository userWriteRepository;

  @Mock
  private UserQueryRepository userQueryRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private RegisterUseCase RegisterUseCase;

  @BeforeEach
  void setUp() {
    RegisterUseCase =
        new RegisterUseCase(userQueryRepository, userWriteRepository, passwordEncoder);
  }

  @Test
  void execute_shouldRegisterUserSuccessfully() {
    RegisterCommand dto =
        new RegisterCommand("testuser", "password123", "Test User", "https://example.com/avatar.png");
    when(userQueryRepository.existsActiveByAccountName(any(AccountName.class))).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
    when(userWriteRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    User result = RegisterUseCase.execute(dto);

    assertNotNull(result);
    assertEquals("testuser", result.getAccountName().value());
    assertEquals("Test User", result.getDisplayName().value());
  }

  @Test
  void execute_shouldThrowValidationExceptionForShortPassword() {
    RegisterCommand dto = new RegisterCommand("testuser", "short", "Test", "avatar");

    assertThrows(ValidationException.class, () -> RegisterUseCase.execute(dto));
  }

  @Test
  void execute_shouldThrowValidationExceptionForBlankAccountName() {
    RegisterCommand dto = new RegisterCommand("   ", "password123", "Test", "avatar");

    assertThrows(ValidationException.class, () -> RegisterUseCase.execute(dto));
  }

  @Test
  void execute_shouldThrowValidationExceptionForNullAccountName() {
    RegisterCommand dto = new RegisterCommand(null, "password123", "Test", "avatar");

    assertThrows(ValidationException.class, () -> RegisterUseCase.execute(dto));
  }

  @Test
  void execute_shouldThrowValidationExceptionForTooLongAccountName() {
    String longName = "a".repeat(257);
    RegisterCommand dto = new RegisterCommand(longName, "password123", "Test", "avatar");

    assertThrows(ValidationException.class, () -> RegisterUseCase.execute(dto));
  }

  @Test
  void execute_shouldThrowResourceAlreadyExistsException() {
    RegisterCommand dto = new RegisterCommand("testuser", "password123", "Test", "avatar");
    when(userQueryRepository.existsActiveByAccountName(any(AccountName.class))).thenReturn(true);

    assertThrows(ResourceAlreadyExistsException.class, () -> RegisterUseCase.execute(dto));
  }
}
