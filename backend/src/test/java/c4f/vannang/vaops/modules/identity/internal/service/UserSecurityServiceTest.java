package c4f.vannang.vaops.modules.identity.internal.service;

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

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserSecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSecurityService userSecurityService;

    @Test
    void handleSuccessfulLogin_ShouldResetFailedLoginCountAndSetLastLoginAt_WhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .accountName("john.doe")
                .failedLoginCount(3)
                .lastLoginAt(null)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(user));

        // Act
        userSecurityService.handleSuccessfulLogin(userId);

        // Assert
        assertEquals(0, user.getFailedLoginCount());
        assertNotNull(user.getLastLoginAt());
        verify(userRepository).save(user);
    }

    @Test
    void handleSuccessfulLogin_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userSecurityService.handleSuccessfulLogin(userId)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handleFailedLogin_ShouldIncrementFailedLoginCount_WhenUserExistsAndBelowLimit() {
        // Arrange
        String accountName = "john.doe";
        User user = User.builder()
                .accountName(accountName)
                .failedLoginCount(2)
                .build();

        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.of(user));

        // Act
        userSecurityService.handleFailedLogin(accountName);

        // Assert
        assertEquals(3, user.getFailedLoginCount());
        assertNull(user.getLockedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void handleFailedLogin_ShouldLockAccount_WhenFailedLoginAttemptsReachMax() {
        // Arrange
        String accountName = "john.doe";
        User user = User.builder()
                .accountName(accountName)
                .failedLoginCount(4) // 4 -> 5 on next failure
                .build();

        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.of(user));

        // Act
        userSecurityService.handleFailedLogin(accountName);

        // Assert
        assertEquals(5, user.getFailedLoginCount());
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(Instant.now()));
        verify(userRepository).save(user);
    }

    @Test
    void handleFailedLogin_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        String accountName = "john.doe";
        when(userRepository.findByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userSecurityService.handleFailedLogin(accountName)
        );
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void isAccountLocked_ShouldReturnTrue_WhenLockedUntilIsInFuture() {
        // Arrange
        User user = User.builder()
                .lockedUntil(Instant.now().plusSeconds(60))
                .build();

        // Act & Assert
        assertTrue(userSecurityService.isAccountLocked(user));
    }

    @Test
    void isAccountLocked_ShouldReturnFalse_WhenLockedUntilIsInPast() {
        // Arrange
        User user = User.builder()
                .lockedUntil(Instant.now().minusSeconds(60))
                .build();

        // Act & Assert
        falseCheck(user);
    }

    @Test
    void isAccountLocked_ShouldReturnFalse_WhenLockedUntilIsNull() {
        // Arrange
        User user = User.builder()
                .lockedUntil(null)
                .build();

        // Act & Assert
        falseCheck(user);
    }

    @Test
    void isAccountLocked_ShouldReturnFalse_WhenUserIsNull() {
        // Act & Assert
        assertFalse(userSecurityService.isAccountLocked(null));
    }

    private void falseCheck(User user) {
        assertFalse(userSecurityService.isAccountLocked(user));
    }
}
