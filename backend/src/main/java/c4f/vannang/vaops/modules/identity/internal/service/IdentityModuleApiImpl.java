package c4f.vannang.vaops.modules.identity.internal.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.api.dto.FindByAccountNameQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.api.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserService;
import c4f.vannang.vaops.modules.identity.internal.usecase.LoginFailedUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.LoginSuccessfulUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.RegisterUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.ToggleStatusUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityModuleApiImpl implements IdentityModuleApi {

  private final RegisterUseCase RegisterUseCase;
  private final SoftDeleteUseCase SoftDeleteUseCase;
  private final ToggleStatusUseCase ToggleStatusUseCase;
  private final UpdateProfileUseCase UpdateProfileUseCase;
  private final ChangePasswordUseCase ChangePasswordUseCase;
  private final LoginSuccessfulUseCase LoginSuccessfulUseCase;
  private final LoginFailedUseCase LoginFailedUseCase;
  private final FindUserService findUserService;
  private final UserDtoMapper userDtoMapper;

  @Override
  public Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query) {
    return findUserService.findForAuth(query)
        .map(userDtoMapper::toAuthDto);
  }

  @Override
  public void recordSuccessfulLogin(RecordSuccessfulLoginCommand command) {
    LoginSuccessfulUseCase.execute(command);
  }

  @Override
  public void recordFailedLogin(RecordFailedLoginCommand command) {
    LoginFailedUseCase.execute(command);
  }

  @Override
  public UserDto register(RegisterDto registerDto) {
    return userDtoMapper.toDto(RegisterUseCase.execute(registerDto));
  }

  @Override
  public void softDelete(SoftDeleteUserCommand command) {
    SoftDeleteUseCase.execute(command);
  }

  @Override
  public void deactivate(ToggleUserStatusCommand command) {
    ToggleStatusUseCase.execute(command);
  }

  @Override
  public void activate(ToggleUserStatusCommand command) {
    ToggleStatusUseCase.execute(command);
  }

  @Override
  public void updateProfile(UpdateProfileCommand command) {
    UpdateProfileUseCase.execute(command);
  }

  @Override
  public void changePassword(ChangePasswordCommand command) {
    ChangePasswordUseCase.execute(command);
  }

  @Override
  public Optional<UserDto> getUserById(FindByIdQuery query) {
    return findUserService.findById(query)
        .map(userDtoMapper::toDto);
  }

  @Override
  public Optional<UserDto> findByAccountName(FindByAccountNameQuery query) {
    return findUserService.findByAccountName(query)
        .map(userDtoMapper::toDto);
  }
}
