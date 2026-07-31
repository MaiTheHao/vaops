package c4f.vannang.vaops.modules.identity.api.service;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;

public interface IdentityProfileService {

  UserDto getProfile(FindByIdQuery query);

  UserDto updateProfile(UpdateProfileRequest request);

  void changePassword(ChangePasswordRequest request);
}
