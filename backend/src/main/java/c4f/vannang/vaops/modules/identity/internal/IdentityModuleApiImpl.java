package c4f.vannang.vaops.modules.identity.internal;

import java.util.Optional;

import org.springframework.stereotype.Service;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByAccountNameQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.SoftDeleteUserRequest;
import c4f.vannang.vaops.modules.identity.api.dto.ToggleUserStatusRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.CheckAvailableUserUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserByAccountNameUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserByIdUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.LoginFailedUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.LoginSuccessfulUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.RegisterUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.ToggleStatusUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class IdentityModuleApiImpl implements IdentityModuleApi {

  private final RegisterUseCase registerUseCase;
  private final SoftDeleteUseCase softDeleteUseCase;
  private final ToggleStatusUseCase toggleStatusUseCase;
  private final UpdateProfileUseCase updateProfileUseCase;
  private final ChangePasswordUseCase changePasswordUseCase;
  private final LoginSuccessfulUseCase loginSuccessfulUseCase;
  private final LoginFailedUseCase loginFailedUseCase;
  private final FindUserByIdUseCase findUserByIdUseCase;
  private final FindUserByAccountNameUseCase findUserByAccountNameUseCase;
  private final CheckAvailableUserUseCase checkAvailableUserUseCase;
  private final UserDtoMapper userDtoMapper;
  private final IdentityMapper identityMapper;

  @Override
  public Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query) {
    FindByAccountNameCommand internalQuery = identityMapper.toInternal(query);
    return findUserByAccountNameUseCase.execute(internalQuery)
        .map(userDtoMapper::toAuthDto);
  }

  @Override
  public void checkAvailableUser(CheckAvailableUserQuery query) {
    CheckAvailableUserCommand internalQuery = identityMapper.toInternal(query);
    checkAvailableUserUseCase.execute(internalQuery);
  }

  @Override
  public void recordSuccessfulLogin(RecordSuccessfulLoginRequest command) {
    RecordSuccessfulLoginCommand internalCommand = identityMapper.toInternal(command);
    loginSuccessfulUseCase.execute(internalCommand);
  }

  @Override
  public void recordFailedLogin(RecordFailedLoginRequest command) {
    RecordFailedLoginCommand internalCommand = identityMapper.toInternal(command);
    loginFailedUseCase.execute(internalCommand);
  }

  @Override
  public UserDto register(RegisterRequest registerDto) {
    RegisterCommand internalCommand = identityMapper.toInternal(registerDto);
    return userDtoMapper.toDto(registerUseCase.execute(internalCommand));
  }

  @Override
  public void softDelete(SoftDeleteUserRequest command) {
    SoftDeleteUserCommand internalCommand = identityMapper.toInternal(command);
    softDeleteUseCase.execute(internalCommand);
  }

  @Override
  public void deactivate(ToggleUserStatusRequest command) {
    ToggleUserStatusCommand internalCommand = identityMapper.toInternal(command);
    toggleStatusUseCase.execute(internalCommand);
  }

  @Override
  public void activate(ToggleUserStatusRequest command) {
    ToggleUserStatusCommand internalCommand = identityMapper.toInternal(command);
    toggleStatusUseCase.execute(internalCommand);
  }

  @Override
  public void updateProfile(UpdateProfileRequest command) {
    UpdateProfileCommand internalCommand = identityMapper.toInternal(command);
    updateProfileUseCase.execute(internalCommand);
  }

  @Override
  public void changePassword(ChangePasswordRequest command) {
    ChangePasswordCommand internalCommand = identityMapper.toInternal(command);
    changePasswordUseCase.execute(internalCommand);
  }

  @Override
  public Optional<UserDto> getUserById(FindByIdQuery query) {
    FindByIdCommand internalQuery = identityMapper.toInternal(query);
    return findUserByIdUseCase.execute(internalQuery)
        .map(userDtoMapper::toDto);
  }

  @Override
  public Optional<UserDto> findByAccountName(FindByAccountNameQuery query) {
    FindByAccountNameCommand internalQuery = identityMapper.toInternal(query);
    return findUserByAccountNameUseCase.execute(internalQuery)
        .map(userDtoMapper::toDto);
  }
}
