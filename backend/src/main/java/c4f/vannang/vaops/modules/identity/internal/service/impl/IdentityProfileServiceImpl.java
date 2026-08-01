package c4f.vannang.vaops.modules.identity.internal.service.impl;

import org.springframework.stereotype.Service;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.api.service.IdentityProfileAPIService;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.mapper.IdentityMapper;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class IdentityProfileServiceImpl implements IdentityProfileAPIService {

  private final UserProfileService userProfileService;
  private final UserDtoMapper userDtoMapper;
  private final IdentityMapper identityMapper;

  @Override
  public UserDto getProfile(FindByIdQuery query) {
    FindByIdCommand internalQuery = identityMapper.toInternal(query);
    return userDtoMapper.toDto(userProfileService.getProfile(internalQuery));
  }

  @Override
  public UserDto updateProfile(UpdateProfileRequest request) {
    UpdateProfileCommand internalCommand = identityMapper.toInternal(request);
    return userDtoMapper.toDto(userProfileService.updateProfile(internalCommand));
  }

  @Override
  public void changePassword(ChangePasswordRequest request) {
    ChangePasswordCommand internalCommand = identityMapper.toInternal(request);
    userProfileService.changePassword(internalCommand);
  }
}
