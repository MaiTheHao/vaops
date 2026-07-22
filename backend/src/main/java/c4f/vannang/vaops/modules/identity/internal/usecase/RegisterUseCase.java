package c4f.vannang.vaops.modules.identity.internal.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUseCase {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;

  public User execute(RegisterCommand dto) {
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
}
