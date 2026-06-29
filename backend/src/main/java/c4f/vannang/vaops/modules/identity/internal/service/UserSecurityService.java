package c4f.vannang.vaops.modules.identity.internal.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;

    @Transactional
    public void handleSuccessfulLogin(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFailedLoginCount(0);
        user.setLastLoginAt(Instant.now());

        userRepository.save(user);
    }

    @Transactional
    public void handleFailedLogin(String accountName) {
        User user = userRepository.findByAccountNameAndDeletedAtIsNull(accountName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int newCount = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(newCount);

        if (newCount >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
        }

        userRepository.save(user);
    }

    public boolean isAccountLocked(User user) {
        if (user == null) {
            return false;
        }
        Instant lockedUntil = user.getLockedUntil();
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }
}
