package c4f.vannang.vaops.modules.identity.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.mapper.UserMapper;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import c4f.vannang.vaops.modules.identity.internal.service.IdentityModuleApiImpl;
import c4f.vannang.vaops.modules.identity.internal.service.UserCommandService;
import c4f.vannang.vaops.modules.identity.internal.service.UserProfileService;
import c4f.vannang.vaops.modules.identity.internal.service.UserSecurityService;

@ExtendWith(MockitoExtension.class)
class IdentityModuleApiImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private UserSecurityService userSecurityService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private IdentityModuleApiImpl identityModuleApi;

    @Test
    void getUserForAuth_ShouldReturnAuthDto_WhenUserExists() {
        String accountName = "test.user";
        User user = new User();
        UserAuthDto authDto = new UserAuthDto(UUID.randomUUID(), "hash", Instant.now(), true);

        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.of(user));
        when(userMapper.toAuthDTO(user)).thenReturn(authDto);

        Optional<UserAuthDto> result = identityModuleApi.getUserForAuth(accountName);

        assertTrue(result.isPresent());
        assertEquals(authDto, result.get());
        verify(userRepository).findByAccountNameAndDeletedAtIsNull(accountName);
        verify(userMapper).toAuthDTO(user);
    }

    @Test
    void getUserForAuth_ShouldReturnEmpty_WhenUserDoesNotExist() {
        String accountName = "nonexistent";

        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.empty());

        Optional<UserAuthDto> result = identityModuleApi.getUserForAuth(accountName);

        assertTrue(result.isEmpty());
        verify(userRepository).findByAccountNameAndDeletedAtIsNull(accountName);
        verifyNoInteractions(userMapper);
    }

    @Test
    void recordSuccessfulLogin_ShouldCallSecurityService() {
        UUID userId = UUID.randomUUID();

        identityModuleApi.recordSuccessfulLogin(userId);

        verify(userSecurityService).handleSuccessfulLogin(userId);
    }

    @Test
    void recordFailedLogin_ShouldCallSecurityService() {
        String accountName = "test.user";

        identityModuleApi.recordFailedLogin(accountName);

        verify(userSecurityService).handleFailedLogin(accountName);
    }

    @Test
    void register_ShouldReturnUserDto_WhenValidInput() {
        String accountName = "new.user";
        String password = "password123";
        String displayName = "New User";
        String avatarUrl = "http://avatar";
        User user = new User();
        UserDto userDto = new UserDto(UUID.randomUUID(), accountName, displayName, avatarUrl, true, null, null, null);
        RegisterDto registerDto = new RegisterDto(accountName, password, displayName, avatarUrl);

        when(userCommandService.register(accountName, password, displayName, avatarUrl)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDto);

        UserDto result = identityModuleApi.register(registerDto);

        assertEquals(userDto, result);
        verify(userCommandService).register(accountName, password, displayName, avatarUrl);
        verify(userMapper).toDTO(user);
    }

    @Test
    void softDelete_ShouldCallCommandService() {
        UUID userId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        identityModuleApi.softDelete(userId, deletedBy);

        verify(userCommandService).softDelete(userId, deletedBy);
    }

    @Test
    void deactivate_ShouldCallCommandServiceWithFalse() {
        UUID userId = UUID.randomUUID();

        identityModuleApi.deactivate(userId);

        verify(userCommandService).toggleActiveStatus(userId, false);
    }

    @Test
    void activate_ShouldCallCommandServiceWithTrue() {
        UUID userId = UUID.randomUUID();

        identityModuleApi.activate(userId);

        verify(userCommandService).toggleActiveStatus(userId, true);
    }

    @Test
    void updateProfile_ShouldCallProfileService() {
        UUID userId = UUID.randomUUID();
        String displayName = "Updated";
        String avatar = "http://new-avatar";

        identityModuleApi.updateProfile(userId, displayName, avatar);

        verify(userProfileService).updateBasicInfo(userId, displayName, avatar);
    }

    @Test
    void changePassword_ShouldCallProfileService() {
        UUID userId = UUID.randomUUID();
        String oldPass = "old";
        String newPass = "new";

        identityModuleApi.changePassword(userId, oldPass, newPass);

        verify(userProfileService).changePassword(userId, oldPass, newPass);
    }

    @Test
    void getUserById_ShouldReturnDto_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        UserDto userDto = new UserDto(userId, "test", "Test", "http://avatar", true, null, null, null);

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDto);

        Optional<UserDto> result = identityModuleApi.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(userDto, result.get());
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(userMapper).toDTO(user);
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        Optional<UserDto> result = identityModuleApi.getUserById(userId);

        assertTrue(result.isEmpty());
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void findByAccountName_ShouldReturnDto_WhenUserExists() {
        String accountName = "test.user";
        User user = new User();
        UserDto userDto = new UserDto(UUID.randomUUID(), accountName, "Test", "http://avatar", true, null, null, null);

        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDto);

        Optional<UserDto> result = identityModuleApi.findByAccountName(accountName);

        assertTrue(result.isPresent());
        assertEquals(userDto, result.get());
        verify(userRepository).findByAccountNameAndDeletedAtIsNull(accountName);
        verify(userMapper).toDTO(user);
    }

    @Test
    void findByAccountName_ShouldReturnEmpty_WhenUserDoesNotExist() {
        String accountName = "nonexistent";

        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.empty());

        Optional<UserDto> result = identityModuleApi.findByAccountName(accountName);

        assertTrue(result.isEmpty());
        verify(userRepository).findByAccountNameAndDeletedAtIsNull(accountName);
        verifyNoInteractions(userMapper);
    }
}
