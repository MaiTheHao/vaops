package c4f.vannang.vaops.modules.identity.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import c4f.vannang.vaops.modules.identity.api.event.UserCreatedEvent;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    void register_ShouldRegisterUser_WhenInputIsValid() {
        // Arrange
        String accountName = "john.doe";
        String rawPassword = "securePassword123";
        String displayName = "John Doe";
        String avatarUrl = "http://example.com/avatar.png";
        String encodedPassword = "encodedPassword";

        when(userRepository.existsByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .accountName(accountName)
                .passwordHash(encodedPassword)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .active(true)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User registeredUser = userCommandService.register(accountName, rawPassword, displayName, avatarUrl);

        // Assert
        assertNotNull(registeredUser);
        assertEquals(savedUser.getId(), registeredUser.getId());
        assertEquals(accountName, registeredUser.getAccountName());
        assertEquals(encodedPassword, registeredUser.getPasswordHash());
        assertEquals(displayName, registeredUser.getDisplayName());
        assertEquals(avatarUrl, registeredUser.getAvatarUrl());
        assertTrue(registeredUser.isActive());

        verify(userRepository).existsByAccountNameAndDeletedAtIsNull(accountName);
        verify(passwordEncoder).encode(rawPassword);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertEquals(accountName, capturedUser.getAccountName());
        assertEquals(encodedPassword, capturedUser.getPasswordHash());
        assertEquals(displayName, capturedUser.getDisplayName());
        assertEquals(avatarUrl, capturedUser.getAvatarUrl());
        assertTrue(capturedUser.isActive());

        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void register_ShouldThrowException_WhenAccountNameIsEmpty() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            userCommandService.register("", "rawPassword", "Display Name", "avatar")
        );
        assertThrows(ValidationException.class, () -> 
            userCommandService.register(null, "rawPassword", "Display Name", "avatar")
        );
    }

    @Test
    void register_ShouldThrowException_WhenPasswordIsTooShort() {
        // Act & Assert
        assertThrows(ValidationException.class, () -> 
            userCommandService.register("john.doe", "short", "Display Name", "avatar")
        );
    }

    @Test
    void register_ShouldThrowException_WhenAccountNameAlreadyExists() {
        // Arrange
        String accountName = "john.doe";
        when(userRepository.existsByAccountNameAndDeletedAtIsNull(accountName)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> 
            userCommandService.register(accountName, "securePassword123", "Display Name", "avatar")
        );
        verify(userRepository).existsByAccountNameAndDeletedAtIsNull(accountName);
        verifyNoMoreInteractions(passwordEncoder, userRepository, eventPublisher);
    }

    @Test
    void softDelete_ShouldSetDeletedFieldsAndDeactivate_WhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .accountName("john.doe")
                .active(true)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userCommandService.softDelete(userId, deletedBy);

        // Assert
        assertFalse(existingUser.isActive());
        assertNotNull(existingUser.getDeletedAt());
        assertEquals(deletedBy, existingUser.getDeletedBy());
        verify(userRepository).save(existingUser);
    }

    @Test
    void softDelete_ShouldThrowException_WhenUserDoesNotExistOrAlreadyDeleted() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            userCommandService.softDelete(userId, deletedBy)
        );
    }

    @Test
    void toggleActiveStatus_ShouldUpdateActiveStatus_WhenUserExists() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User existingUser = User.builder()
                .id(userId)
                .accountName("john.doe")
                .active(true)
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.of(existingUser));

        // Act
        userCommandService.toggleActiveStatus(userId, false);

        // Assert
        assertFalse(existingUser.isActive());
        verify(userRepository).save(existingUser);
    }

    @Test
    void toggleActiveStatus_ShouldThrowException_WhenUserDoesNotExist() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedAtIsNull(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            userCommandService.toggleActiveStatus(userId, false)
        );
    }
}
