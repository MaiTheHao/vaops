package c4f.vannang.vaops.module.identity.service.impl;

import c4f.vannang.vaops.module.identity.domain.User;
import c4f.vannang.vaops.module.identity.dto.CreateUserDto;
import c4f.vannang.vaops.module.identity.dto.PartialUpdateUserDto;
import c4f.vannang.vaops.module.identity.dto.SafeUserDto;
import c4f.vannang.vaops.module.identity.dto.UpdateUserDto;
import c4f.vannang.vaops.module.identity.mapper.UserMapper;
import c4f.vannang.vaops.module.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_Success() {
        CreateUserDto dto = CreateUserDto.builder()
                .email("test@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .password("password123")
                .build();

        User user = User.builder()
                .email("test@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .build();

        SafeUserDto safeDto = SafeUserDto.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .isActive(true)
                .build();

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByPhone(dto.phone())).thenReturn(false);
        when(userMapper.toUser(dto)).thenReturn(user);
        when(passwordEncoder.encode(dto.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toSafeUserDto(any(User.class))).thenReturn(safeDto);

        SafeUserDto result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals(safeDto.email(), result.email());
        assertEquals(safeDto.fullName(), result.fullName());
        assertTrue(result.isActive());

        verify(userRepository).existsByEmail(dto.email());
        verify(userRepository).existsByPhone(dto.phone());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ThrowsIllegalArgumentException_WhenEmailExists() {
        CreateUserDto dto = CreateUserDto.builder()
                .email("test@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });

        assertTrue(exception.getMessage().contains("Email is already in use"));
        verify(userRepository).existsByEmail(dto.email());
        verify(userRepository, never()).existsByPhone(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ThrowsIllegalArgumentException_WhenPhoneExists() {
        CreateUserDto dto = CreateUserDto.builder()
                .email("test@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByPhone(dto.phone())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(dto);
        });

        assertTrue(exception.getMessage().contains("Phone number is already in use"));
        verify(userRepository).existsByEmail(dto.email());
        verify(userRepository).existsByPhone(dto.phone());
        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_ReturnsSafeUserDto_WhenUserExistsAndNotDeleted() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .email("test@example.com")
                .fullName("John Doe")
                .build();
        SafeUserDto safeDto = SafeUserDto.builder()
                .id(id)
                .email("test@example.com")
                .fullName("John Doe")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toSafeUserDto(user)).thenReturn(safeDto);

        Optional<SafeUserDto> result = userService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(safeDto, result.get());
        verify(userRepository).findById(id);
    }

    @Test
    void findById_ReturnsEmpty_WhenUserIsDeleted() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .email("test@example.com")
                .fullName("John Doe")
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<SafeUserDto> result = userService.findById(id);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(id);
        verify(userMapper, never()).toSafeUserDto(any());
    }

    @Test
    void findById_ReturnsEmpty_WhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<SafeUserDto> result = userService.findById(id);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(id);
        verify(userMapper, never()).toSafeUserDto(any());
    }

    @Test
    void findByEmail_ReturnsSafeUserDto_WhenUserExistsAndNotDeleted() {
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .fullName("John Doe")
                .build();
        SafeUserDto safeDto = SafeUserDto.builder()
                .email(email)
                .fullName("John Doe")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toSafeUserDto(user)).thenReturn(safeDto);

        Optional<SafeUserDto> result = userService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(safeDto, result.get());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_ReturnsEmpty_WhenUserIsDeleted() {
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .fullName("John Doe")
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<SafeUserDto> result = userService.findByEmail(email);

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByPhone_ReturnsSafeUserDto_WhenUserExistsAndNotDeleted() {
        String phone = "1234567890";
        User user = User.builder()
                .phone(phone)
                .fullName("John Doe")
                .build();
        SafeUserDto safeDto = SafeUserDto.builder()
                .phone(phone)
                .fullName("John Doe")
                .build();

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));
        when(userMapper.toSafeUserDto(user)).thenReturn(safeDto);

        Optional<SafeUserDto> result = userService.findByPhone(phone);

        assertTrue(result.isPresent());
        assertEquals(safeDto, result.get());
        verify(userRepository).findByPhone(phone);
    }

    @Test
    void findByPhone_ReturnsEmpty_WhenUserIsDeleted() {
        String phone = "1234567890";
        User user = User.builder()
                .phone(phone)
                .fullName("John Doe")
                .deletedAt(Instant.now())
                .build();

        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));

        Optional<SafeUserDto> result = userService.findByPhone(phone);

        assertTrue(result.isEmpty());
        verify(userRepository).findByPhone(phone);
    }

    @Test
    void findAll_ReturnsOnlyNonDeletedUsers() {
        User user1 = User.builder().email("user1@example.com").build();
        User user2 = User.builder().email("user2@example.com").deletedAt(Instant.now()).build();
        User user3 = User.builder().email("user3@example.com").build();

        SafeUserDto dto1 = SafeUserDto.builder().email("user1@example.com").build();
        SafeUserDto dto3 = SafeUserDto.builder().email("user3@example.com").build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));
        when(userMapper.toSafeUserDto(user1)).thenReturn(dto1);
        when(userMapper.toSafeUserDto(user3)).thenReturn(dto3);

        List<SafeUserDto> result = userService.findAll();

        assertEquals(2, result.size());
        assertEquals("user1@example.com", result.get(0).email());
        assertEquals("user3@example.com", result.get(1).email());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        UUID id = UUID.randomUUID();
        UpdateUserDto dto = UpdateUserDto.builder()
                .email("new@example.com")
                .phone("0987654321")
                .fullName("John Updated")
                .build();

        User user = User.builder()
                .email("old@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .build();

        SafeUserDto safeDto = SafeUserDto.builder()
                .id(id)
                .email("new@example.com")
                .phone("0987654321")
                .fullName("John Updated")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByPhone(dto.phone())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toSafeUserDto(user)).thenReturn(safeDto);

        SafeUserDto result = userService.updateUser(id, dto);

        assertNotNull(result);
        assertEquals(safeDto.email(), result.email());
        assertEquals(safeDto.fullName(), result.fullName());
        verify(userRepository).findById(id);
        verify(userRepository).existsByEmail(dto.email());
        verify(userRepository).existsByPhone(dto.phone());
        verify(userMapper).updateUserFromDto(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ThrowsIllegalArgumentException_WhenUserNotFound() {
        UUID id = UUID.randomUUID();
        UpdateUserDto dto = UpdateUserDto.builder().build();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(id, dto));
        verify(userRepository).findById(id);
    }

    @Test
    void updateUser_ThrowsIllegalArgumentException_WhenEmailExists() {
        UUID id = UUID.randomUUID();
        UpdateUserDto dto = UpdateUserDto.builder().email("exists@example.com").build();
        User user = User.builder().email("old@example.com").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(id, dto));
    }

    @Test
    void updateUser_ThrowsIllegalArgumentException_WhenPhoneExists() {
        UUID id = UUID.randomUUID();
        UpdateUserDto dto = UpdateUserDto.builder().phone("9999999999").build();
        User user = User.builder().phone("1111111111").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone(dto.phone())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(id, dto));
    }

    @Test
    void partialUpdateUser_Success() {
        UUID id = UUID.randomUUID();
        PartialUpdateUserDto dto = PartialUpdateUserDto.builder()
                .fullName("John Partial")
                .build();

        User user = User.builder()
                .email("old@example.com")
                .phone("1234567890")
                .fullName("John Doe")
                .build();

        SafeUserDto safeDto = SafeUserDto.builder()
                .id(id)
                .email("old@example.com")
                .phone("1234567890")
                .fullName("John Partial")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toSafeUserDto(user)).thenReturn(safeDto);

        SafeUserDto result = userService.partialUpdateUser(id, dto);

        assertNotNull(result);
        assertEquals(safeDto.fullName(), result.fullName());
        verify(userRepository).findById(id);
        verify(userMapper).partialUpdateUserFromDto(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_Success() {
        UUID id = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();
        User user = User.builder()
                .email("test@example.com")
                .isActive(true)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deleteUser(id, deletedBy);

        assertNotNull(user.getDeletedAt());
        assertEquals(deletedBy, user.getDeletedBy());
        assertFalse(user.isActive());
        verify(userRepository).findById(id);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_ThrowsIllegalArgumentException_WhenUserNotFound() {
        UUID id = UUID.randomUUID();
        UUID deletedBy = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(id, deletedBy));
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void activateUser_Success() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .email("test@example.com")
                .isActive(false)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.activateUser(id);

        assertTrue(user.isActive());
        verify(userRepository).findById(id);
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_ThrowsIllegalArgumentException_WhenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.activateUser(id));
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_Success() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .email("test@example.com")
                .isActive(true)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deactivateUser(id);

        assertFalse(user.isActive());
        verify(userRepository).findById(id);
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_ThrowsIllegalArgumentException_WhenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.deactivateUser(id));
        verify(userRepository).findById(id);
        verify(userRepository, never()).save(any());
    }
}
