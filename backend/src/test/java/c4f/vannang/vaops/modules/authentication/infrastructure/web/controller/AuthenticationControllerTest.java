package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import c4f.vannang.vaops.core.env.AppProperties;
import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.core.env.AuthProperties.Jwt;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.LoginWebRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.service.AuthenticationService;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private static final String ACCOUNT_NAME = "user";
    private static final String PASSWORD = "password";
    private static final String DISPLAY_NAME = "Display Name";
    private static final String AVATAR_URL = "avatar-url";
    private static final String ACCESS_TOKEN = "access-token-123";
    private static final String REFRESH_TOKEN = "refresh-token-123";
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    @Mock
    private AuthenticationService authenticationService;
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
    void login_ShouldSetCookiesAndReturnOk_WhenCredentialsAreValid() {
        // given
        LoginWebRequestDto webRequest = new LoginWebRequestDto(ACCOUNT_NAME, PASSWORD);
        LoginCommandResult commandResult = new LoginCommandResult(ACCESS_TOKEN, REFRESH_TOKEN);
        when(authenticationService.login(new LoginCommand(ACCOUNT_NAME, PASSWORD)))
            .thenReturn(commandResult);

        // when
        ResponseEntity<Void> response = controller.login(webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNotNull();
        verify(authenticationService).login(new LoginCommand(ACCOUNT_NAME, PASSWORD));
    }

    @Test
    void register_ShouldReturnCreatedWithBody_WhenRegistrationSucceeds() {
        // given
        RegisterWebRequestDto webRequest =
            new RegisterWebRequestDto(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL);
        UUID generatedId = UUID.randomUUID();
        RegisterCommandResult commandResult =
            new RegisterCommandResult(generatedId, ACCOUNT_NAME, DISPLAY_NAME, AVATAR_URL);
        when(authenticationService.register(
                new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL)))
            .thenReturn(commandResult);

        // when
        ResponseEntity<RegisterWebResponseDto> response = controller.register(webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(generatedId);
        assertThat(response.getBody().accountName()).isEqualTo(ACCOUNT_NAME);
        assertThat(response.getBody().displayName()).isEqualTo(DISPLAY_NAME);
        assertThat(response.getBody().avatarUrl()).isEqualTo(AVATAR_URL);
        verify(authenticationService)
            .register(new RegisterCommand(ACCOUNT_NAME, PASSWORD, DISPLAY_NAME, AVATAR_URL));
    }

    @Test
    void refresh_ShouldSetCookiesAndReturnOk_WhenRefreshTokenCookieExists() {
        // given
        HttpServletRequest request = requestWithRefreshCookie(REFRESH_TOKEN);
        RefreshTokenCommandResult commandResult =
            new RefreshTokenCommandResult("new-access-token", "new-refresh-token");
        when(authenticationService.refreshToken(new RefreshTokenCommand(REFRESH_TOKEN)))
            .thenReturn(commandResult);

        // when
        ResponseEntity<Void> response = controller.refresh(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNotNull();
        verify(authenticationService).refreshToken(new RefreshTokenCommand(REFRESH_TOKEN));
    }

    @Test
    void logout_ShouldRevokeAndClearCookies_WhenRefreshTokenCookieExists() {
        // given
        HttpServletRequest request = requestWithRefreshCookie(REFRESH_TOKEN);

        // when
        ResponseEntity<Void> response = controller.logout(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNotNull();
        verify(authenticationService).logout(new LogoutCommand(REFRESH_TOKEN));
    }

    @Test
    void refresh_ShouldThrowUnauthenticatedException_WhenRefreshTokenCookieIsMissing() {
        // given
        HttpServletRequest request = requestWithRefreshCookie(null);

        // when
        // then
        assertThatThrownBy(() -> controller.refresh(request))
            .isInstanceOf(UnauthenticatedException.class)
            .hasMessage("Refresh token is missing");
        verify(authenticationService, never()).refreshToken(new RefreshTokenCommand(REFRESH_TOKEN));
    }

    @Test
    void logout_ShouldOnlyClearCookies_WhenRefreshTokenCookieIsMissing() {
        // given
        HttpServletRequest request = requestWithRefreshCookie(null);

        // when
        ResponseEntity<Void> response = controller.logout(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).isNotNull();
        verify(authenticationService, never()).logout(new LogoutCommand(REFRESH_TOKEN));
    }

    @Test
    void refresh_ShouldThrowUnauthenticatedException_WhenRequestCookiesIsNull() {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> controller.refresh(request))
            .isInstanceOf(UnauthenticatedException.class)
            .hasMessage("Refresh token is missing");
        verify(authenticationService, never()).refreshToken(new RefreshTokenCommand(REFRESH_TOKEN));
    }

    private HttpServletRequest requestWithRefreshCookie(String refreshTokenValue) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        if (refreshTokenValue == null) {
            when(request.getCookies()).thenReturn(new Cookie[] {});
        } else {
            Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refreshTokenValue);
            when(request.getCookies()).thenReturn(new Cookie[] {cookie});
        }
        return request;
    }
}