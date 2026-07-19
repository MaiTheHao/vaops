package c4f.vannang.vaops.modules.authentication.internal.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

  @Mock
  private IdentityModuleApi identityModuleApi;

  private RegisterUseCase useCase;

  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    useCase = new RegisterUseCase(identityModuleApi);
  }

  @Test
  void execute_shouldRegisterSuccessfully() {
    RegisterCommand command = new RegisterCommand(
        "newuser", "password123", "New User", "http://avatar.url"
    );

    UserDto mockUserDto = new UserDto(
        userId, "newuser", "New User", "http://avatar.url", true, null, null, null
    );

    when(identityModuleApi.register(any(RegisterRequest.class))).thenReturn(mockUserDto);

    RegisterCommandResult result = useCase.execute(command);

    assertNotNull(result);
    assertEquals(userId, result.id());
    assertEquals("newuser", result.accountName());
    assertEquals("New User", result.displayName());
    assertEquals("http://avatar.url", result.avatarUrl());

    verify(identityModuleApi, times(1)).register(any(RegisterRequest.class));
  }

  @Test
  void execute_shouldThrowValidationException_whenIdentityApiThrowsIt() {
    RegisterCommand command = new RegisterCommand("user", "pass", "Name", null);

    when(identityModuleApi.register(any(RegisterRequest.class)))
        .thenThrow(new ValidationException("Invalid input"));

    assertThrows(ValidationException.class, () -> useCase.execute(command));
  }

  @Test
  void execute_shouldThrowResourceAlreadyExistsException_whenAccountExists() {
    RegisterCommand command = new RegisterCommand("existinguser", "pass", "Name", null);

    when(identityModuleApi.register(any(RegisterRequest.class)))
        .thenThrow(new ResourceAlreadyExistsException("User already exists"));

    assertThrows(ResourceAlreadyExistsException.class, () -> useCase.execute(command));
  }

  @Test
  void execute_shouldWrapInInternalServerException_whenUnexpectedError() {
    RegisterCommand command = new RegisterCommand("user", "pass", "Name", null);

    when(identityModuleApi.register(any(RegisterRequest.class)))
        .thenThrow(new RuntimeException("Database down"));

    InternalServerException exception = assertThrows(InternalServerException.class,
        () -> useCase.execute(command));
    assertTrue(exception.getMessage().contains("Unexpected error"));
  }
}
