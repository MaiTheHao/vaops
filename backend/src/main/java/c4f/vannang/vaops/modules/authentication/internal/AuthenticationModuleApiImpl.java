package c4f.vannang.vaops.modules.authentication.internal;

import c4f.vannang.vaops.modules.authentication.api.AuthenticationModuleApi;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.usecase.LoginUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.LogoutUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.RefreshTokenUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class AuthenticationModuleApiImpl implements AuthenticationModuleApi {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AuthMapper mapper;

    @Override
    public LoginResponseDto login(LoginRequestDto dto) {
        LoginCommand command = mapper.toInternal(dto);
        LoginCommandResult result = loginUseCase.execute(command);
        return mapper.toApiResponse(result);
    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto dto) {
        RegisterCommand command = mapper.toInternal(dto);
        RegisterCommandResult result = registerUseCase.execute(command);
        return mapper.toApiResponse(result);
    }

    @Override
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request) {
        RefreshTokenCommand command = mapper.toInternal(request);
        RefreshTokenCommandResult result = refreshTokenUseCase.execute(command);
        return mapper.toApiResponse(result);
    }

    @Override
    public LogoutResponseDto logout(LogoutRequestDto request) {
        LogoutCommand command = mapper.toInternal(request);
        LogoutCommandResult result = logoutUseCase.execute(command);
        return mapper.toApiResponse(result);
    }
}
