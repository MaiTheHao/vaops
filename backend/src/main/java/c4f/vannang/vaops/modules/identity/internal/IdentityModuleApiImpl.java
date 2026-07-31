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
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.modules.identity.internal.dto.UserSearchCriteria;
import c4f.vannang.vaops.modules.identity.internal.mapper.IdentityMapper;

import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class IdentityModuleApiImpl implements IdentityModuleApi {

  private final UserService userService;
  private final UserProfileService userProfileService;
  private final UserDtoMapper userDtoMapper;
  private final IdentityMapper identityMapper;

  @Override
  public Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query) {
    FindByAccountNameCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserByAccountName(internalQuery)
        .map(userDtoMapper::toAuthDto);
  }

  @Override
  public void checkAvailableUser(CheckAvailableUserQuery query) {
    CheckAvailableUserCommand internalQuery = identityMapper.toInternal(query);
    userService.checkAvailableUser(internalQuery);
  }

  @Override
  public void recordSuccessfulLogin(RecordSuccessfulLoginRequest command) {
    RecordSuccessfulLoginCommand internalCommand = identityMapper.toInternal(command);
    userService.recordSuccessfulLogin(internalCommand);
  }

  @Override
  public void recordFailedLogin(RecordFailedLoginRequest command) {
    RecordFailedLoginCommand internalCommand = identityMapper.toInternal(command);
    userService.recordFailedLogin(internalCommand);
  }

  @Override
  public UserDto register(RegisterRequest registerDto) {
    RegisterCommand internalCommand = identityMapper.toInternal(registerDto);
    return userDtoMapper.toDto(userService.register(internalCommand));
  }

  @Override
  public void softDelete(SoftDeleteUserRequest command) {
    SoftDeleteUserCommand internalCommand = identityMapper.toInternal(command);
    userService.softDelete(internalCommand);
  }

  @Override
  public void deactivate(ToggleUserStatusRequest command) {
    ToggleUserStatusCommand internalCommand = identityMapper.toInternal(command);
    userService.toggleStatus(internalCommand);
  }

  @Override
  public void activate(ToggleUserStatusRequest command) {
    ToggleUserStatusCommand internalCommand = identityMapper.toInternal(command);
    userService.toggleStatus(internalCommand);
  }

  @Override
  public void updateProfile(UpdateProfileRequest command) {
    UpdateProfileCommand internalCommand = identityMapper.toInternal(command);
    userProfileService.updateProfile(internalCommand);
  }

  @Override
  public void changePassword(ChangePasswordRequest command) {
    ChangePasswordCommand internalCommand = identityMapper.toInternal(command);
    userProfileService.changePassword(internalCommand);
  }

  @Override
  public Optional<UserDto> getUserById(FindByIdQuery query) {
    FindByIdCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserById(internalQuery)
        .map(userDtoMapper::toDto);
  }

  @Override
  public Optional<UserDto> findByAccountName(FindByAccountNameQuery query) {
    FindByAccountNameCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserByAccountName(internalQuery)
        .map(userDtoMapper::toDto);
  }

  @Override
  public PageResponse<UserDto> searchUsers(UserSearchCriteria criteria) {
    Page<User> userPage = userService.searchUsers(criteria);
    return PageResponse.from(userPage, userDtoMapper::toDto);
  }
}
