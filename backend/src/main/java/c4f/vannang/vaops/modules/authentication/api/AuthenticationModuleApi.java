package c4f.vannang.vaops.modules.authentication.api;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;

public interface AuthenticationModuleApi {
    LoginCommandResultDto login(LoginCommandDto dto);
    RegisterCommandResultDto register(RegisterCommandDto dto);
    RefreshTokenCommandResultDto refreshToken(RefreshTokenCommandDto command);
}
