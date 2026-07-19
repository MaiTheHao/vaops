package c4f.vannang.vaops.modules.authentication.internal;

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

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    LoginCommand toInternal(LoginRequestDto dto);
    LoginResponseDto toApiResponse(LoginCommandResult result);

    RegisterCommand toInternal(RegisterRequestDto dto);
    RegisterResponseDto toApiResponse(RegisterCommandResult result);

    RefreshTokenCommand toInternal(RefreshTokenRequestDto dto);
    RefreshTokenResponseDto toApiResponse(RefreshTokenCommandResult result);

    LogoutCommand toInternal(LogoutRequestDto dto);
    LogoutResponseDto toApiResponse(LogoutCommandResult result);
}
