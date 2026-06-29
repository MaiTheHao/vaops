package c4f.vannang.vaops.modules.identity.internal.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.api.event.UserCreatedEvent;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import c4f.vannang.vaops.shared.exception.ValidationException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public User register(String accountName, String rawPassword, String displayName, String avatarUrl) {
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new ValidationException("Account name cannot be empty");
        }
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long");
        }

        if (userRepository.existsByAccountNameAndDeletedAtIsNull(accountName)) {
            throw new ResourceAlreadyExistsException("Account name already exists");
        }

        User user = User.builder()
                .accountName(accountName)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        eventPublisher.publishEvent(new UserCreatedEvent(savedUser.getId(), savedUser.getAccountName()));

        return savedUser;
    }

    @Transactional
    public void softDelete(UUID userId, UUID deletedBy) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found or already deleted"));

        user.setDeletedAt(Instant.now());
        user.setDeletedBy(deletedBy);
        user.setActive(false);

        userRepository.save(user);
    }

    @Transactional
    public void toggleActiveStatus(UUID userId, boolean active) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(active);

        userRepository.save(user);
    }
}
