package c4f.vannang.vaops.modules.authentication.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.usecase.LoginUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.LogoutUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.RefreshTokenUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.RegisterUseCase;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterResponseDto;
import c4f.vannang.vaops.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IdentityModuleApi identityModuleApi;

    @InjectMocks
    private RegisterUseCase RegisterUseCase;

    @Test
    void register_ShouldReturnResponse_WhenRegistrationSucceeds() {
        RegisterCommand request = new RegisterCommand("john.doe", "password123", "John Doe", "avatar");
        UUID id = UUID.randomUUID();
        UserDto mockUserDto = new UserDto(id, "john.doe", "John Doe", "avatar", true, null, null, null);

        when(identityModuleApi.register(any(RegisterRequest.class))).thenReturn(mockUserDto);

        RegisterCommandResult response = RegisterUseCase.execute(request);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("john.doe", response.accountName());
        assertEquals("John Doe", response.displayName());
        assertEquals("avatar", response.avatarUrl());
    }

    @Test
    void register_ShouldPropagateValidationException_WhenInvalidInput() {
        RegisterCommand request = new RegisterCommand("", "password123", "John Doe", "avatar");
        when(identityModuleApi.register(any(RegisterRequest.class))).thenThrow(new ValidationException("Validation error"));

        assertThrows(ValidationException.class, () -> RegisterUseCase.execute(request));
    }

    @Test
    void authenticationModuleApiImpl_ShouldDelegateLoginToLoginUseCase() {
        var loginUseCase = mock(LoginUseCase.class);
        var registerUseCaseMock = mock(RegisterUseCase.class);
        var authMapperMock = mock(AuthMapper.class);
        var api = new AuthenticationModuleApiImpl(loginUseCase, registerUseCaseMock, mock(RefreshTokenUseCase.class), mock(LogoutUseCase.class), authMapperMock);

        LoginRequestDto dto = new LoginRequestDto("user", "pass");
        LoginCommand command = new LoginCommand("user", "pass");
        LoginCommandResult commandResult = new LoginCommandResult("access", "refresh");
        LoginResponseDto expected = new LoginResponseDto("access", "refresh");

        when(authMapperMock.toInternal(dto)).thenReturn(command);
        when(loginUseCase.execute(command)).thenReturn(commandResult);
        when(authMapperMock.toApiResponse(commandResult)).thenReturn(expected);

        LoginResponseDto result = api.login(dto);

        assertSame(expected, result);
        verify(loginUseCase).execute(command);
    }

    @Test
    void authenticationModuleApiImpl_ShouldDelegateRegisterToRegisterUseCase() {
        var loginUseCase = mock(LoginUseCase.class);
        var registerUseCaseMock = mock(RegisterUseCase.class);
        var authMapperMock = mock(AuthMapper.class);
        var api = new AuthenticationModuleApiImpl(loginUseCase, registerUseCaseMock, mock(RefreshTokenUseCase.class), mock(LogoutUseCase.class), authMapperMock);

        RegisterRequestDto dto = new RegisterRequestDto("user", "pass", "User", "av");
        RegisterCommand command = new RegisterCommand("user", "pass", "User", "av");
        UUID randomId = UUID.randomUUID();
        RegisterCommandResult commandResult = new RegisterCommandResult(randomId, "user", "User", "av");
        RegisterResponseDto expected = new RegisterResponseDto(randomId, "user", "User", "av");

        when(authMapperMock.toInternal(dto)).thenReturn(command);
        when(registerUseCaseMock.execute(command)).thenReturn(commandResult);
        when(authMapperMock.toApiResponse(commandResult)).thenReturn(expected);

        RegisterResponseDto result = api.register(dto);

        assertSame(expected, result);
        verify(registerUseCaseMock).execute(command);
    }
}
