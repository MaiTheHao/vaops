package c4f.vannang.vaops.module.identity.service.impl;

import c4f.vannang.vaops.module.identity.domain.User;
import c4f.vannang.vaops.module.identity.domain.UserCredential;
import c4f.vannang.vaops.module.identity.dto.CreateUserDto;
import c4f.vannang.vaops.module.identity.dto.PartialUpdateUserDto;
import c4f.vannang.vaops.module.identity.dto.SafeUserDto;
import c4f.vannang.vaops.module.identity.dto.UpdateUserDto;
import c4f.vannang.vaops.module.identity.repository.UserRepository;
import c4f.vannang.vaops.module.identity.service.UserService;
import c4f.vannang.vaops.module.identity.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SafeUserDto createUser(CreateUserDto createUserDto) {
        if (createUserDto.email() != null && userRepository.existsByEmail(createUserDto.email())) {
            throw new IllegalArgumentException("Email is already in use: " + createUserDto.email());
        }
        if (createUserDto.phone() != null && userRepository.existsByPhone(createUserDto.phone())) {
            throw new IllegalArgumentException("Phone number is already in use: " + createUserDto.phone());
        }

        User user = userMapper.toUser(createUserDto);
        user.setActive(true);

        UserCredential credential = UserCredential.builder()
                .user(user)
                .passwordHash(passwordEncoder.encode(createUserDto.password()))
                .passwordChangedAt(Instant.now())
                .failedLoginCount(0)
                .build();

        user.setCredential(credential);

        User savedUser = userRepository.save(user);
        return userMapper.toSafeUserDto(savedUser);
    }

    @Override
    public Optional<SafeUserDto> findById(UUID id) {
        return userRepository.findById(id)
                .filter(user -> user.getDeletedAt() == null)
                .map(userMapper::toSafeUserDto);
    }

    @Override
    public Optional<SafeUserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getDeletedAt() == null)
                .map(userMapper::toSafeUserDto);
    }

    @Override
    public Optional<SafeUserDto> findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .filter(user -> user.getDeletedAt() == null)
                .map(userMapper::toSafeUserDto);
    }

    @Override
    public List<SafeUserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(userMapper::toSafeUserDto)
                .toList();
    }

    @Override
    @Transactional
    public SafeUserDto updateUser(UUID id, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (updateUserDto.email() != null && !updateUserDto.email()
                .equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserDto.email())) {
                throw new IllegalArgumentException("Email is already in use: " + updateUserDto.email());
            }
        }

        if (updateUserDto.phone() != null && !updateUserDto.phone()
                .equals(user.getPhone())) {
            if (userRepository.existsByPhone(updateUserDto.phone())) {
                throw new IllegalArgumentException("Phone number is already in use: " + updateUserDto.phone());
            }
        }

        userMapper.updateUserFromDto(updateUserDto, user);

        User savedUser = userRepository.save(user);
        return userMapper.toSafeUserDto(savedUser);
    }

    @Override
    @Transactional
    public SafeUserDto partialUpdateUser(UUID id, PartialUpdateUserDto partialUpdateUserDto) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (partialUpdateUserDto.email() != null && !partialUpdateUserDto.email()
                .equals(user.getEmail())) {
            if (userRepository.existsByEmail(partialUpdateUserDto.email())) {
                throw new IllegalArgumentException("Email is already in use: " + partialUpdateUserDto.email());
            }
        }

        if (partialUpdateUserDto.phone() != null && !partialUpdateUserDto.phone()
                .equals(user.getPhone())) {
            if (userRepository.existsByPhone(partialUpdateUserDto.phone())) {
                throw new IllegalArgumentException("Phone number is already in use: " + partialUpdateUserDto.phone());
            }
        }

        userMapper.partialUpdateUserFromDto(partialUpdateUserDto, user);

        User savedUser = userRepository.save(user);
        return userMapper.toSafeUserDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id, UUID deletedBy) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setDeletedAt(Instant.now());
        user.setDeletedBy(deletedBy);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }
}
