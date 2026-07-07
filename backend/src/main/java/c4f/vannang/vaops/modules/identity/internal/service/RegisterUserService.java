package c4f.vannang.vaops.modules.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.repository.UserWriteRepository;
import c4f.vannang.vaops.modules.identity.internal.infrastructure.mapper.UserDtoMapper;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterUserService {

  private final UserQueryRepository userQueryRepository;
  private final UserWriteRepository userWriteRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDtoMapper userDtoMapper;

  public UserDto execute(RegisterDto dto) {
    if (dto.rawPassword() == null || dto.rawPassword().length() < 8) {
      throw new ValidationException("Password must be at least 8 characters long");
    }

    if (dto.accountName() == null || dto.accountName().isBlank()) {
      throw new ValidationException("Account name cannot be blank");
    }

    String trimmedAccount = dto.accountName().strip();
    if (trimmedAccount.length() > 256) {
      throw new ValidationException("Account name must not exceed 256 characters");
    }

    if (userQueryRepository.existsByAccountNameAndDeletedAtIsNull(trimmedAccount)) {
      throw new ResourceAlreadyExistsException("Account name already exists");
    }

    String passwordHash = passwordEncoder.encode(dto.rawPassword());
    User user = User.register(trimmedAccount, passwordHash, dto.displayName(), dto.avatarUrl());

    User saved = userWriteRepository.save(user);
    return userDtoMapper.toDto(saved);
  }
}
