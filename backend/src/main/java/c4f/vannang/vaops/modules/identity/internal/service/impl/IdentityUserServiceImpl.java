package c4f.vannang.vaops.modules.identity.internal.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.mapper.IdentityMapper;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class IdentityUserServiceImpl implements IdentityUserAPIService {

  private final UserService userService;
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
  public Optional<UserDto> getUserById(FindByIdQuery query) {
    FindByIdCommand internalQuery = identityMapper.toInternal(query);
    return userService.findUserById(internalQuery)
        .map(userDtoMapper::toDto);
  }
}
