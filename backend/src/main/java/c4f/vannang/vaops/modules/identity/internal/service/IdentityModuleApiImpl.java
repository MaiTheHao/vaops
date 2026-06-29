package c4f.vannang.vaops.modules.identity.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.modules.identity.internal.mapper.UserMapper;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IdentityModuleApiImpl implements IdentityModuleApi {

    private final UserRepository userRepository;
    private final UserCommandService userCommandService;
    private final UserProfileService userProfileService;
    private final UserSecurityService userSecurityService;
    private final UserMapper userMapper;

    @Override
    public Optional<UserAuthDto> getUserForAuth(String accountName) {
        return userRepository.findByAccountNameAndDeletedAtIsNull(accountName).map(userMapper::toAuthDTO);
    }

    @Override
    public void recordSuccessfulLogin(UUID userId) {
        userSecurityService.handleSuccessfulLogin(userId);
    }

    @Override
    public void recordFailedLogin(String accountName) {
        userSecurityService.handleFailedLogin(accountName);
    }

    @Override
    public UserDto register(RegisterDto registerDto) {
        return userMapper.toDTO(userCommandService.register(registerDto.accountName(), registerDto.rawPassword(),
                registerDto.displayName(), registerDto.avatarUrl()));
    }

    @Override
    public void softDelete(UUID userId, UUID deletedBy) {
        userCommandService.softDelete(userId, deletedBy);
    }

    @Override
    public void deactivate(UUID userId) {
        userCommandService.toggleActiveStatus(userId, false);
    }

    @Override
    public void activate(UUID userId) {
        userCommandService.toggleActiveStatus(userId, true);
    }

    @Override
    public void updateProfile(UUID userId, String displayName, String avatarUrl) {
        userProfileService.updateBasicInfo(userId, displayName, avatarUrl);
    }

    @Override
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        userProfileService.changePassword(userId, oldPassword, newPassword);
    }

    @Override
    public Optional<UserDto> getUserById(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId).map(userMapper::toDTO);
    }

    @Override
    public Optional<UserDto> findByAccountName(String accountName) {
        return userRepository.findByAccountNameAndDeletedAtIsNull(accountName).map(userMapper::toDTO);
    }
}
