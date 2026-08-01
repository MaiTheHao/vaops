package c4f.vannang.vaops.modules.authentication.internal.service.impl;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterResponseDto;
import c4f.vannang.vaops.modules.authentication.api.service.AuthenticationAPIService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class AuthenticationAPIServiceImpl implements AuthenticationAPIService {

    private final AuthenticationService authenticationService;
    private final AuthMapper mapper;

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        LoginCommand command = mapper.toInternal(dto);
        LoginCommandResult result = authenticationService.login(command);
        return mapper.toApiResponse(result);
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto dto) {
        RegisterCommand command = mapper.toInternal(dto);
        RegisterCommandResult result = authenticationService.register(command);
        return mapper.toApiResponse(result);
    }

    @Override
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        RefreshTokenCommand command = mapper.toInternal(request);
        RefreshTokenCommandResult result = authenticationService.refreshToken(command);
        return mapper.toApiResponse(result);
    }

    @Override
    public LogoutResponseDto logout(LogoutRequestDto request) {
        LogoutCommand command = mapper.toInternal(request);
        LogoutCommandResult result = authenticationService.logout(command);
        return mapper.toApiResponse(result);
    }
}
