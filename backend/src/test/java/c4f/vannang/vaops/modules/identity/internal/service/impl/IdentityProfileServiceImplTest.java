package c4f.vannang.vaops.modules.identity.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.mapper.IdentityMapper;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdentityProfileServiceImplTest {

  @Mock
  private UserProfileService userProfileService;

  @Mock
  private UserDtoMapper userDtoMapper;

  @Mock
  private IdentityMapper identityMapper;

  @InjectMocks
  private IdentityProfileAPIServiceImpl identityProfileService;

  @Test
  void getProfile_ShouldReturnUserDto_WhenUserExists() {
    // given
    UUID userId = UUID.randomUUID();
    FindByIdQuery query = new FindByIdQuery(userId);
    FindByIdCommand internalQuery = new FindByIdCommand(userId);
    User user = createUser();
    UserDto expectedDto = new UserDto(userId, "john_doe", "John", "avatar", true, null, null, null);

    when(identityMapper.toInternal(query)).thenReturn(internalQuery);
    when(userProfileService.getProfile(internalQuery)).thenReturn(user);
    when(userDtoMapper.toDto(user)).thenReturn(expectedDto);

    // when
    UserDto result = identityProfileService.getProfile(query);

    // then
    assertThat(result).isEqualTo(expectedDto);
    verify(userProfileService).getProfile(internalQuery);
  }

  @Test
  void updateProfile_ShouldReturnUpdatedUserDto_WhenProfileIsUpdated() {
    // given
    UUID userId = UUID.randomUUID();
    UpdateProfileRequest request = new UpdateProfileRequest(userId, "New Name", "new_avatar");
    UpdateProfileCommand internalCommand = new UpdateProfileCommand(userId, "New Name", "new_avatar");
    User user = createUser();
    UserDto expectedDto = new UserDto(userId, "john_doe", "New Name", "new_avatar", true, null, null, null);

    when(identityMapper.toInternal(request)).thenReturn(internalCommand);
    when(userProfileService.updateProfile(internalCommand)).thenReturn(user);
    when(userDtoMapper.toDto(user)).thenReturn(expectedDto);

    // when
    UserDto result = identityProfileService.updateProfile(request);

    // then
    assertThat(result).isEqualTo(expectedDto);
    verify(userProfileService).updateProfile(internalCommand);
  }

  @Test
  void changePassword_ShouldDelegateToUserProfileService_WhenRequestIsValid() {
    // given
    UUID userId = UUID.randomUUID();
    ChangePasswordRequest request = new ChangePasswordRequest(userId, "oldPass", "newPass");
    ChangePasswordCommand internalCommand = new ChangePasswordCommand(userId, "oldPass", "newPass");

    when(identityMapper.toInternal(request)).thenReturn(internalCommand);

    // when
    identityProfileService.changePassword(request);

    // then
    verify(userProfileService).changePassword(internalCommand);
  }

  private User createUser() {
    return User.register(
        new AccountName("john_doe"),
        new PasswordHash("hashed-password"),
        new DisplayName("John"),
        new AvatarUrl("avatar"));
  }
}