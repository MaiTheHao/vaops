package c4f.vannang.vaops.modules.authentication.internal.service;

import c4f.vannang.vaops.modules.authentication.api.AuthenticationModuleApi;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;
import c4f.vannang.vaops.modules.authentication.internal.usecase.LoginUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.LogoutUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.RefreshTokenUseCase;
import c4f.vannang.vaops.modules.authentication.internal.usecase.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationModuleApiImpl implements AuthenticationModuleApi {

    private final LoginUseCase LoginUseCase;
    private final RegisterUseCase RegisterUseCase;
    private final RefreshTokenUseCase RefreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @Override
    public LoginCommandResultDto login(LoginCommandDto dto) {
        return LoginUseCase.execute(dto);
    }

    @Override
    public RegisterCommandResultDto register(RegisterCommandDto dto) {
        return RegisterUseCase.execute(dto);
    }

    @Override
    public RefreshTokenCommandResultDto refreshToken(RefreshTokenCommandDto command) {
        return RefreshTokenUseCase.execute(command);
    }

    @Override
    public LogoutCommandResultDto logout(LogoutCommandDto command) {
        return logoutUseCase.execute(command);
    }
}
