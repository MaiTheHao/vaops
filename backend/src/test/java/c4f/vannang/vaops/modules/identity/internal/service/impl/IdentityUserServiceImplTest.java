package c4f.vannang.vaops.modules.identity.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import c4f.vannang.vaops.modules.authorization.api.service.AuthorizationAPIService;
import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.mapper.UserDtoMapper;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.mapper.IdentityMapper;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdentityUserServiceImplTest {

  private static final String ACCOUNT_NAME = "john_doe";
  private static final String PASSWORD_HASH = "hashed-password";
  private static final String DISPLAY_NAME = "John Doe";
  private static final String AVATAR_URL = "https://example.com/avatar.png";

  @Mock
  private UserService userService;

  @Mock
  private UserDtoMapper userDtoMapper;

  @Mock
  private IdentityMapper identityMapper;

  @Mock
  private AuthorizationAPIService authorizationApiService;

  @InjectMocks
  private IdentityUserAPIServiceImpl identityUserService;

  @Test
  void getUserForAuth_ShouldReturnUserAuthDto_WhenUserExists() {
    // given
    FindForAuthQuery query = new FindForAuthQuery(ACCOUNT_NAME);
    FindByAccountNameCommand internalQuery = new FindByAccountNameCommand(ACCOUNT_NAME);
    User user = createUser();
    UserAuthDto expectedDto = new UserAuthDto(UUID.randomUUID(), PASSWORD_HASH, null, true);

    when(identityMapper.toInternal(query)).thenReturn(internalQuery);
    when(userService.findUserByAccountName(internalQuery)).thenReturn(Optional.of(user));
    when(userDtoMapper.toAuthDto(user)).thenReturn(expectedDto);

    // when
    Optional<UserAuthDto> result = identityUserService.getUserForAuth(query);

    // then
    assertThat(result).contains(expectedDto);
    verify(userService).findUserByAccountName(internalQuery);
  }

  @Test
  void getUserForAuth_ShouldReturnEmpty_WhenUserNotFound() {
    // given
    FindForAuthQuery query = new FindForAuthQuery(ACCOUNT_NAME);
    FindByAccountNameCommand internalQuery = new FindByAccountNameCommand(ACCOUNT_NAME);

    when(identityMapper.toInternal(query)).thenReturn(internalQuery);
    when(userService.findUserByAccountName(internalQuery)).thenReturn(Optional.empty());

    // when
    Optional<UserAuthDto> result = identityUserService.getUserForAuth(query);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  void checkAvailableUser_ShouldDelegateToUserService() {
    // given
    UUID userId = UUID.randomUUID();
    CheckAvailableUserQuery query = new CheckAvailableUserQuery(userId);
    CheckAvailableUserCommand internalQuery = new CheckAvailableUserCommand(userId);

    when(identityMapper.toInternal(query)).thenReturn(internalQuery);

    // when
    identityUserService.checkAvailableUser(query);

    // then
    verify(userService).checkAvailableUser(internalQuery);
  }

  @Test
  void recordSuccessfulLogin_ShouldDelegateToUserService() {
    // given
    UUID userId = UUID.randomUUID();
    RecordSuccessfulLoginRequest request = new RecordSuccessfulLoginRequest(userId);
    RecordSuccessfulLoginCommand internalCommand = new RecordSuccessfulLoginCommand(userId);

    when(identityMapper.toInternal(request)).thenReturn(internalCommand);

    // when
    identityUserService.recordSuccessfulLogin(request);

    // then
    verify(userService).recordSuccessfulLogin(internalCommand);
  }

  @Test
  void recordFailedLogin_ShouldDelegateToUserService() {
    // given
    RecordFailedLoginRequest request = new RecordFailedLoginRequest(ACCOUNT_NAME);
    RecordFailedLoginCommand internalCommand = new RecordFailedLoginCommand(ACCOUNT_NAME);

    when(identityMapper.toInternal(request)).thenReturn(internalCommand);

    // when
    identityUserService.recordFailedLogin(request);

    // then
    verify(userService).recordFailedLogin(internalCommand);
  }

  @Test
  void register_ShouldReturnUserDto_WhenSuccessful() {
    // given
    RegisterRequest request = new RegisterRequest(ACCOUNT_NAME, PASSWORD_HASH, DISPLAY_NAME, AVATAR_URL);
    RegisterCommand internalCommand = new RegisterCommand(ACCOUNT_NAME, PASSWORD_HASH, DISPLAY_NAME, AVATAR_URL);
    User user = createUser();
    UserDto expectedDto = new UserDto(UUID.randomUUID(), ACCOUNT_NAME, DISPLAY_NAME, AVATAR_URL, true, null, null, null);

    when(identityMapper.toInternal(request)).thenReturn(internalCommand);
    when(userService.register(internalCommand)).thenReturn(user);
    when(userDtoMapper.toDto(user)).thenReturn(expectedDto);

    // when
    UserDto result = identityUserService.register(request);

    // then
    assertThat(result).isEqualTo(expectedDto);
    verify(userService).register(internalCommand);
    verify(authorizationApiService).assignDefaultRoleToUser(user.getId());
  }

  @Test
  void getUserById_ShouldReturnUserDto_WhenUserExists() {
    // given
    UUID userId = UUID.randomUUID();
    FindByIdQuery query = new FindByIdQuery(userId);
    FindByIdCommand internalQuery = new FindByIdCommand(userId);
    User user = createUser();
    UserDto expectedDto = new UserDto(userId, ACCOUNT_NAME, DISPLAY_NAME, AVATAR_URL, true, null, null, null);

    when(identityMapper.toInternal(query)).thenReturn(internalQuery);
    when(userService.findUserById(internalQuery)).thenReturn(Optional.of(user));
    when(userDtoMapper.toDto(user)).thenReturn(expectedDto);

    // when
    Optional<UserDto> result = identityUserService.getUserById(query);

    // then
    assertThat(result).contains(expectedDto);
    verify(userService).findUserById(internalQuery);
  }

  @Test
  void getUserById_ShouldReturnEmpty_WhenUserNotFound() {
    // given
    UUID userId = UUID.randomUUID();
    FindByIdQuery query = new FindByIdQuery(userId);
    FindByIdCommand internalQuery = new FindByIdCommand(userId);

    when(identityMapper.toInternal(query)).thenReturn(internalQuery);
    when(userService.findUserById(internalQuery)).thenReturn(Optional.empty());

    // when
    Optional<UserDto> result = identityUserService.getUserById(query);

    // then
    assertThat(result).isEmpty();
  }

  private User createUser() {
    return User.register(
        new AccountName(ACCOUNT_NAME),
        new PasswordHash(PASSWORD_HASH),
        new DisplayName(DISPLAY_NAME),
        new AvatarUrl(AVATAR_URL));
  }
}