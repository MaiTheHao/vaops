package c4f.vannang.vaops.modules.identity.api.service;

import java.util.Optional;
import java.util.UUID;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;

public interface IdentityModuleApi {
    // Auth support
    Optional<UserAuthDto> getUserForAuth(String accountName);

    void recordSuccessfulLogin(UUID userId);

    void recordFailedLogin(String accountName);

    // Lifecycle / Write
    UserDto register(RegisterDto registerDto);

    void softDelete(UUID userId, UUID deletedBy);

    void deactivate(UUID userId);

    void activate(UUID userId);

    // Profile
    void updateProfile(UUID userId, String displayName, String avatarUrl);

    void changePassword(UUID userId, String oldPassword, String newPassword);

    // Queries
    Optional<UserDto> getUserById(UUID userId);

    Optional<UserDto> findByAccountName(String accountName);
}
