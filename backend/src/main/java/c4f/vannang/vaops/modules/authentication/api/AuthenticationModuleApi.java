package c4f.vannang.vaops.modules.authentication.api;

import c4f.vannang.vaops.modules.authentication.api.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LogoutResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenResponseDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterResponseDto;

public interface AuthenticationModuleApi {
    LoginResponseDto login(LoginRequestDto request);
    RegisterResponseDto register(RegisterRequestDto request);
    RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto request);
    LogoutResponseDto logout(LogoutRequestDto request);
}
