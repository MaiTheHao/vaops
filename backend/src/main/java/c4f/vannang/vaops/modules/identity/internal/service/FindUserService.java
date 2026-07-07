package c4f.vannang.vaops.modules.identity.internal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.internal.repository.UserQueryRepository;
import c4f.vannang.vaops.modules.identity.internal.infrastructure.mapper.UserDtoMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindUserService {

  private final UserQueryRepository userQueryRepository;
  private final UserDtoMapper userDtoMapper;

  public Optional<UserDto> findById(UUID userId) {
    return userQueryRepository.findByIdAndDeletedAtIsNull(userId)
        .map(userDtoMapper::toDto);
  }

  public Optional<UserDto> findByAccountName(String accountName) {
    if (accountName == null) return Optional.empty();
    return userQueryRepository.findByAccountNameAndDeletedAtIsNull(accountName.strip())
        .map(userDtoMapper::toDto);
  }

  public Optional<UserAuthDto> findForAuth(String accountName) {
    if (accountName == null) return Optional.empty();
    return userQueryRepository.findByAccountNameAndDeletedAtIsNull(accountName.strip())
        .map(userDtoMapper::toAuthDto);
  }
}
