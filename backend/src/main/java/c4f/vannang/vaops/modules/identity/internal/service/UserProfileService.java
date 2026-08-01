package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;

public interface UserProfileService {
  User getProfile(FindByIdCommand command);
  User updateProfile(UpdateProfileCommand command);
  void changePassword(ChangePasswordCommand command);
}
