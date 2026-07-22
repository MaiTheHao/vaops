package c4f.vannang.vaops.modules.identity.internal.usecase;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckAvailableUserUseCase {

  private final UserQueryRepository userQueryRepository;

  public void execute(CheckAvailableUserCommand command) {
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
}
