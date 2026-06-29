package c4f.vannang.vaops.modules.identity.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.BusinessRuleViolationException;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void updateBasicInfo_ShouldUpdateDisplayNameAndAvatar_WhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .displayName("Old Name")
                .avatarUrl("http://old.url")
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        // Act
        userProfileService.updateBasicInfo(userId, "New Name", "http://new.url");

        // Assert
        assertEquals("New Name", user.getDisplayName());
        assertEquals("http://new.url", user.getAvatarUrl());
        verify(userRepository).save(user);
    }

    @Test
    void updateBasicInfo_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userProfileService.updateBasicInfo(userId, "New Name", "http://new.url")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenValidInputs() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String oldPassword = "oldPassword123";
        String newPassword = "newSecurePassword123";
        String oldPasswordHash = "oldHash";
        String newPasswordHash = "newHash";

        User user = User.builder()
                .id(userId)
                .passwordHash(oldPasswordHash)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, oldPasswordHash)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newPasswordHash);

        // Act
        userProfileService.changePassword(userId, oldPassword, newPassword);

        // Assert
        assertEquals(newPasswordHash, user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrowException_WhenNewPasswordIsTooShort() {
        // Act & Assert
        assertThrows(ValidationException.class, () ->
            userProfileService.changePassword(UUID.randomUUID(), "oldPass", "short")
        );
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void changePassword_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userProfileService.changePassword(userId, "oldPass", "newPassword123")
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowException_WhenOldPasswordDoesNotMatch() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String oldPassword = "wrongOldPassword";
        String currentHash = "currentHash";

        User user = User.builder()
                .id(userId)
                .passwordHash(currentHash)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, currentHash)).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessRuleViolationException.class, () ->
            userProfileService.changePassword(userId, oldPassword, "newPassword123")
        );
        verify(userRepository, never()).save(any(User.class));
    }
}
