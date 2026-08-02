package c4f.vannang.vaops.modules.authentication.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.AuthMapper;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationAPIServiceImplTest {

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private AuthMapper authMapper;

  @InjectMocks
  private AuthenticationAPIServiceImpl apiService;

  @Test
  void login_ShouldDelegateToAuthenticationService_WhenRequestIsValid() {
    // given
    LoginRequestDto dto = new LoginRequestDto("user", "pass");
    LoginCommand command = new LoginCommand("user", "pass");
    LoginCommandResult commandResult = new LoginCommandResult("access", "refresh");
    LoginResponseDto expected = new LoginResponseDto("access", "refresh");
    when(authMapper.toInternal(dto)).thenReturn(command);
    when(authenticationService.login(command)).thenReturn(commandResult);
    when(authMapper.toApiResponse(commandResult)).thenReturn(expected);

    // when
    LoginResponseDto result = apiService.login(dto);

    // then
    assertThat(result).isSameAs(expected);
    verify(authenticationService).login(command);
  }

  @Test
  void register_ShouldDelegateToAuthenticationService_WhenRequestIsValid() {
    // given
    RegisterRequestDto dto = new RegisterRequestDto("user", "pass", "User", "av");
    RegisterCommand command = new RegisterCommand("user", "pass", "User", "av");
    UUID id = UUID.randomUUID();
    RegisterCommandResult commandResult = new RegisterCommandResult(id, "user", "User", "av");
    RegisterResponseDto expected = new RegisterResponseDto(id, "user", "User", "av");
    when(authMapper.toInternal(dto)).thenReturn(command);
    when(authenticationService.register(command)).thenReturn(commandResult);
    when(authMapper.toApiResponse(commandResult)).thenReturn(expected);

    // when
    RegisterResponseDto result = apiService.register(dto);

    // then
    assertThat(result).isSameAs(expected);
    verify(authenticationService).register(command);
  }

  @Test
  void refreshToken_ShouldDelegateToAuthenticationService_WhenRequestIsValid() {
    // given
    RefreshTokenRequestDto dto = new RefreshTokenRequestDto("refresh");
    RefreshTokenCommand command = new RefreshTokenCommand("refresh");
    RefreshTokenCommandResult commandResult = new RefreshTokenCommandResult("access", "refresh");
    RefreshTokenResponseDto expected = new RefreshTokenResponseDto("access", "refresh");
    when(authMapper.toInternal(dto)).thenReturn(command);
    when(authenticationService.refreshToken(command)).thenReturn(commandResult);
    when(authMapper.toApiResponse(commandResult)).thenReturn(expected);

    // when
    RefreshTokenResponseDto result = apiService.refreshToken(dto);

    // then
    assertThat(result).isSameAs(expected);
    verify(authenticationService).refreshToken(command);
  }

  @Test
  void logout_ShouldDelegateToAuthenticationService_WhenRequestIsValid() {
    // given
    LogoutRequestDto dto = new LogoutRequestDto("refresh");
    LogoutCommand command = new LogoutCommand("refresh");
    LogoutCommandResult commandResult = new LogoutCommandResult(true);
    LogoutResponseDto expected = new LogoutResponseDto(true);
    when(authMapper.toInternal(dto)).thenReturn(command);
    when(authenticationService.logout(command)).thenReturn(commandResult);
    when(authMapper.toApiResponse(commandResult)).thenReturn(expected);

    // when
    LogoutResponseDto result = apiService.logout(dto);

    // then
    assertThat(result).isSameAs(expected);
    verify(authenticationService).logout(command);
  }
}