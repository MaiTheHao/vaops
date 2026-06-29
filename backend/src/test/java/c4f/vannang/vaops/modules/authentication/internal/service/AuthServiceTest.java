package c4f.vannang.vaops.modules.authentication.internal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private IdentityModuleApi identityModuleApi;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnResponse_WhenRegistrationSucceeds() {
        RegisterCommandDto request = new RegisterCommandDto("john.doe", "password123", "John Doe", "avatar");
        UUID id = UUID.randomUUID();
        UserDto mockUserDto = new UserDto(id, "john.doe", "John Doe", "avatar", true, null, null, null);

        when(identityModuleApi.register(any(RegisterDto.class))).thenReturn(mockUserDto);

        RegisterCommandResultDto response = authService.register(request);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("john.doe", response.accountName());
        assertEquals("John Doe", response.displayName());
        assertEquals("avatar", response.avatarUrl());
    }

    @Test
    void register_ShouldPropagateValidationException_WhenInvalidInput() {
        RegisterCommandDto request = new RegisterCommandDto("", "password123", "John Doe", "avatar");
        when(identityModuleApi.register(any(RegisterDto.class))).thenThrow(new ValidationException("Validation error"));

        assertThrows(ValidationException.class, () -> authService.register(request));
    }
}
