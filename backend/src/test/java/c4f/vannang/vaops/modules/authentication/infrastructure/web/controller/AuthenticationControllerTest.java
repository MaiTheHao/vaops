package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.core.env.AppProperties;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.LoginWebRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebResponseDto;
import c4f.vannang.vaops.core.config.AuthProperties;
import c4f.vannang.vaops.core.config.AuthProperties.Jwt;
import c4f.vannang.vaops.modules.authentication.internal.dto.*;
import c4f.vannang.vaops.modules.authentication.internal.usecase.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private LoginUseCase loginUseCase;
    @Mock
    private RegisterUseCase registerUseCase;
    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;
    @Mock
    private LogoutUseCase logoutUseCase;
    @Mock
    private AppProperties appProperties;
    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        Jwt jwt = new Jwt();
        jwt.setAccessExpirationMs(3600000L);
        jwt.setRefreshExpirationMs(86400000L);
        lenient().when(authProperties.getJwt()).thenReturn(jwt);
        lenient().when(appProperties.isProd()).thenReturn(false);
    }

    @Test
    void login_ShouldSetCookiesAndReturnOk() {
        LoginWebRequestDto webRequest = new LoginWebRequestDto("user", "password");
        LoginCommandResult commandResult = new LoginCommandResult("access-token-123", "refresh-token-123");

        when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(commandResult);

        ResponseEntity<Void> response = controller.login(webRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE));
        verify(loginUseCase).execute(new LoginCommand("user", "password"));
    }

    @Test
    void register_ShouldReturnCreatedWithBody() {
        RegisterWebRequestDto webRequest = new RegisterWebRequestDto("user", "password", "Display Name", "avatar-url");
        UUID generatedId = UUID.randomUUID();
        RegisterCommandResult commandResult = new RegisterCommandResult(generatedId, "user", "Display Name", "avatar-url");

        when(registerUseCase.execute(any(RegisterCommand.class))).thenReturn(commandResult);

        ResponseEntity<RegisterWebResponseDto> response = controller.register(webRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(generatedId, response.getBody().id());
        assertEquals("user", response.getBody().accountName());
        assertEquals("Display Name", response.getBody().displayName());
        assertEquals("avatar-url", response.getBody().avatarUrl());
        verify(registerUseCase).execute(new RegisterCommand("user", "password", "Display Name", "avatar-url"));
    }

    @Test
    void refresh_ShouldSetCookiesAndReturnOk_WhenCookieExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refresh_token", "stored-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        RefreshTokenCommandResult commandResult = new RefreshTokenCommandResult("new-access-token", "new-refresh-token");
        when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class))).thenReturn(commandResult);

        ResponseEntity<Void> response = controller.refresh(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE));
        verify(refreshTokenUseCase).execute(new RefreshTokenCommand("stored-refresh-token"));
    }

    @Test
    void logout_ShouldRevokeAndClearCookies() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refresh_token", "stored-refresh-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ResponseEntity<Void> response = controller.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst(org.springframework.http.HttpHeaders.SET_COOKIE));
        verify(logoutUseCase).execute(new LogoutCommand("stored-refresh-token"));
    }
}
