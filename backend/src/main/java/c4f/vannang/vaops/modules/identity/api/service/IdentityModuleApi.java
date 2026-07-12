package c4f.vannang.vaops.modules.identity.api.service;

import java.util.Optional;
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

public interface IdentityModuleApi {
    Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query);
    void recordSuccessfulLogin(RecordSuccessfulLoginCommand command);
    void recordFailedLogin(RecordFailedLoginCommand command);
    UserDto register(RegisterDto registerDto);
    void softDelete(SoftDeleteUserCommand command);
    void deactivate(ToggleUserStatusCommand command);
    void activate(ToggleUserStatusCommand command);
    void updateProfile(UpdateProfileCommand command);
    void changePassword(ChangePasswordCommand command);
    Optional<UserDto> getUserById(FindByIdQuery query);
    Optional<UserDto> findByAccountName(FindByAccountNameQuery query);
}
