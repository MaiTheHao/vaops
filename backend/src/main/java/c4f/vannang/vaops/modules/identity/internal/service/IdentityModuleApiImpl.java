package c4f.vannang.vaops.modules.identity.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityModuleApiImpl implements IdentityModuleApi {

  private final RegisterUserService registerUserService;
  private final SoftDeleteUserService softDeleteUserService;
  private final ToggleUserStatusService toggleUserStatusService;
  private final UpdateUserProfileService updateUserProfileService;
  private final ChangePasswordService changePasswordService;
  private final HandleSuccessfulLoginService handleSuccessfulLoginService;
  private final HandleFailedLoginService handleFailedLoginService;
  private final FindUserService findUserService;

  @Override
  public Optional<UserAuthDto> getUserForAuth(String accountName) {
    return findUserService.findForAuth(accountName);
  }

  @Override
  public void recordSuccessfulLogin(UUID userId) {
    handleSuccessfulLoginService.execute(userId);
  }

  @Override
  public void recordFailedLogin(String accountName) {
    handleFailedLoginService.execute(accountName);
  }

  @Override
  public UserDto register(RegisterDto registerDto) {
    return registerUserService.execute(registerDto);
  }

  @Override
  public void softDelete(UUID userId, UUID deletedBy) {
    softDeleteUserService.execute(userId, deletedBy);
  }

  @Override
  public void deactivate(UUID userId) {
    toggleUserStatusService.execute(userId, false);
  }

  @Override
  public void activate(UUID userId) {
    toggleUserStatusService.execute(userId, true);
  }

  @Override
  public void updateProfile(UUID userId, String displayName, String avatarUrl) {
    updateUserProfileService.execute(userId, displayName, avatarUrl);
  }

  @Override
  public void changePassword(UUID userId, String oldPassword, String newPassword) {
    changePasswordService.execute(userId, oldPassword, newPassword);
  }

  @Override
  public Optional<UserDto> getUserById(UUID userId) {
    return findUserService.findById(userId);
  }

  @Override
  public Optional<UserDto> findByAccountName(String accountName) {
    return findUserService.findByAccountName(accountName);
  }
}
