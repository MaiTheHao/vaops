package c4f.vannang.vaops.modules.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class HandleFailedLoginService {

  public static final int MAX_FAILED_ATTEMPTS = 5;
  private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;

  public void execute(String accountName) {
    if (accountName == null) {
      throw new IllegalArgumentException("Account name must not be null");
    }
    User user = userQueryRepository.findActiveByAccountName(new AccountName(accountName))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.recordFailedLogin(MAX_FAILED_ATTEMPTS, LOCK_DURATION);
    userWriteRepository.save(user);
  }
}
