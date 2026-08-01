package c4f.vannang.vaops.modules.authentication.internal.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.AuthMapper;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.service.AuthenticationService;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IdentityUserAPIService identityUserService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_ShouldReturnResponse_WhenRegistrationSucceeds() {
        RegisterCommand request = new RegisterCommand("john.doe", "password123", "John Doe", "avatar");
        UUID id = UUID.randomUUID();
        UserDto mockUserDto = new UserDto(id, "john.doe", "John Doe", "avatar", true, null, null, null);

        when(identityUserService.register(any(RegisterRequest.class))).thenReturn(mockUserDto);

        RegisterCommandResult response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("john.doe", response.accountName());
        assertEquals("John Doe", response.displayName());
        assertEquals("avatar", response.avatarUrl());
    }

    @Test
    void register_ShouldPropagateValidationException_WhenInvalidInput() {
        RegisterCommand request = new RegisterCommand("", "password123", "John Doe", "avatar");
        when(identityUserService.register(any(RegisterRequest.class))).thenThrow(new ValidationException("Validation error"));

        assertThrows(ValidationException.class, () -> authenticationService.register(request));
    }

    @Test
    void authenticationModuleApiImpl_ShouldDelegateLoginToAuthenticationService() {
        var authenticationServiceMock = mock(AuthenticationService.class);
        var authMapperMock = mock(AuthMapper.class);
        var api = new AuthenticationAPIServiceImpl(authenticationServiceMock, authMapperMock);

        LoginRequestDto dto = new LoginRequestDto("user", "pass");
        LoginCommand command = new LoginCommand("user", "pass");
        LoginCommandResult commandResult = new LoginCommandResult("access", "refresh");
        LoginResponseDto expected = new LoginResponseDto("access", "refresh");

        when(authMapperMock.toInternal(dto)).thenReturn(command);
        when(authenticationServiceMock.login(command)).thenReturn(commandResult);
        when(authMapperMock.toApiResponse(commandResult)).thenReturn(expected);

        LoginResponseDto result = api.login(dto);

        assertSame(expected, result);
        verify(authenticationServiceMock).login(command);
    }

    @Test
    void authenticationModuleApiImpl_ShouldDelegateRegisterToAuthenticationService() {
        var authenticationServiceMock = mock(AuthenticationService.class);
        var authMapperMock = mock(AuthMapper.class);
        var api = new AuthenticationAPIServiceImpl(authenticationServiceMock, authMapperMock);

        RegisterRequestDto dto = new RegisterRequestDto("user", "pass", "User", "av");
        RegisterCommand command = new RegisterCommand("user", "pass", "User", "av");
        UUID randomId = UUID.randomUUID();
        RegisterCommandResult commandResult = new RegisterCommandResult(randomId, "user", "User", "av");
        RegisterResponseDto expected = new RegisterResponseDto(randomId, "user", "User", "av");

        when(authMapperMock.toInternal(dto)).thenReturn(command);
        when(authenticationServiceMock.register(command)).thenReturn(commandResult);
        when(authMapperMock.toApiResponse(commandResult)).thenReturn(expected);

        RegisterResponseDto result = api.register(dto);

        assertSame(expected, result);
        verify(authenticationServiceMock).register(command);
    }
}
