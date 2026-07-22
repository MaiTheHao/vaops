package c4f.vannang.vaops.modules.identity.api.service;

import java.util.Optional;

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
import java.util.UUID;

public interface IdentityModuleApi {
    Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query);
    void checkAvailableUser(CheckAvailableUserQuery query);
    default void checkAvailableUser(UUID userId) {
        checkAvailableUser(new CheckAvailableUserQuery(userId));
    }
    void recordSuccessfulLogin(RecordSuccessfulLoginRequest command);
    void recordFailedLogin(RecordFailedLoginRequest command);
    UserDto register(RegisterRequest registerDto);
    void softDelete(SoftDeleteUserRequest command);
    void deactivate(ToggleUserStatusRequest command);
    void activate(ToggleUserStatusRequest command);
    void updateProfile(UpdateProfileRequest command);
    void changePassword(ChangePasswordRequest command);
    Optional<UserDto> getUserById(FindByIdQuery query);
    Optional<UserDto> findByAccountName(FindByAccountNameQuery query);
}
