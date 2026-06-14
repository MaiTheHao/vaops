package c4f.vannang.vaops.module.identity.service;

import c4f.vannang.vaops.module.identity.dto.CreateUserDto;
import c4f.vannang.vaops.module.identity.dto.PartialUpdateUserDto;
import c4f.vannang.vaops.module.identity.dto.SafeUserDto;
import c4f.vannang.vaops.module.identity.dto.UpdateUserDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    /**
     * Creates a new user using CreateUserDto and returns SafeUserDto.
     * Throws IllegalArgumentException if email or phone is already registered.
     */
    SafeUserDto createUser(CreateUserDto createUserDto);

    /**
     * Finds a user by ID and returns SafeUserDto.
     */
    Optional<SafeUserDto> findById(UUID id);

    /**
     * Finds a user by email and returns SafeUserDto.
     */
    Optional<SafeUserDto> findByEmail(String email);

    /**
     * Finds a user by phone number and returns SafeUserDto.
     */
    Optional<SafeUserDto> findByPhone(String phone);

    /**
     * Lists all non-deleted users as SafeUserDto.
     */
    List<SafeUserDto> findAll();

    /**
     * Performs a full update on user details using UpdateUserDto and returns SafeUserDto.
     */
    SafeUserDto updateUser(UUID id, UpdateUserDto updateUserDto);

    /**
     * Performs a partial update on user details using PartialUpdateUserDto and returns SafeUserDto.
     */
    SafeUserDto partialUpdateUser(UUID id, PartialUpdateUserDto partialUpdateUserDto);

    /**
     * Performs a soft delete on a user by setting deletedAt and deletedBy.
     */
    void deleteUser(UUID id, UUID deletedBy);

    /**
     * Activates a user account.
     */
    void activateUser(UUID id);

    /**
     * Deactivates a user account.
     */
    void deactivateUser(UUID id);
}
