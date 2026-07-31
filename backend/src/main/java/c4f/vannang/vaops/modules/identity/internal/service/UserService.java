package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.*;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.spec.UserSpecification;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  public static final int MAX_FAILED_ATTEMPTS = 5;
  private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  public User register(RegisterCommand dto) {
    User.validatePasswordStrength(dto.rawPassword());

    AccountName accountName = new AccountName(dto.accountName());

    if (userQueryRepository.existsByAccountName(accountName)) {
      throw new ResourceAlreadyExistsException("Account name already exists");
    }

    String passwordHash = passwordEncoder.encode(dto.rawPassword());
    DisplayName displayName = dto.displayName() != null ? new DisplayName(dto.displayName()) : null;
    AvatarUrl avatarUrl = dto.avatarUrl() != null ? new AvatarUrl(dto.avatarUrl()) : null;

    User user = User.register(accountName, new PasswordHash(passwordHash), displayName, avatarUrl);

    return userWriteRepository.save(user);
  }

  @Transactional(readOnly = true)
  public Page<User> searchUsers(UserSearchCriteria criteria) {
    return userQueryRepository.findAll(
        UserSpecification.search(criteria),
        criteria != null ? criteria.toPageable() : org.springframework.data.domain.Pageable.unpaged()
    );
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserById(FindByIdCommand command) {
    return userQueryRepository.findById(command.userId());
  }

  @Transactional(readOnly = true)
  public Optional<User> findUserByAccountName(FindByAccountNameCommand command) {
    if (command.accountName() == null) return Optional.empty();
    return userQueryRepository.findByAccountName(new AccountName(command.accountName()));
  }

  @Transactional(readOnly = true)
  public void checkAvailableUser(CheckAvailableUserCommand command) {
    if (command == null || command.userId() == null) {
      throw new UnauthenticatedException("Invalid user identity");
    }

    User user = userQueryRepository
        .findById(command.userId())
        .orElseThrow(() -> new UnauthenticatedException("User not found: " + command.userId()));

    if (!user.isActive()) {
      throw new UnauthenticatedException("Account is deactivated");
    }

    if (user.isLocked()) {
      throw new AccountLockedException("Account is locked until " + user.getLockedUntil());
    }
  }

  public void softDelete(SoftDeleteUserCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found or already deleted"));

    user.softDelete(command.deletedBy());
    userWriteRepository.save(user);
  }

  public void toggleStatus(ToggleUserStatusCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (command.active()) {
      user.activate();
    } else {
      user.deactivate();
    }

    userWriteRepository.save(user);
  }

  public void recordSuccessfulLogin(RecordSuccessfulLoginCommand command) {
    User user = userQueryRepository.findById(command.userId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.recordSuccessfulLogin();
    userWriteRepository.save(user);
  }

  public void recordFailedLogin(RecordFailedLoginCommand command) {
    if (command.accountName() == null) {
      throw new IllegalArgumentException("Account name must not be null");
    }
    User user = userQueryRepository.findByAccountName(new AccountName(command.accountName()))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.recordFailedLogin(MAX_FAILED_ATTEMPTS, LOCK_DURATION);
    userWriteRepository.save(user);
  }
}
