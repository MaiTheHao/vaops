package c4f.vannang.vaops.modules.identity.api.mapper;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;

@Component
public class UserDtoMapper {

  public UserDto toDto(User user) {
    if (user == null) return null;
    return new UserDto(
        user.getId(),
        user.getAccountName() != null ? user.getAccountName().value() : null,
        user.getDisplayName() != null ? user.getDisplayName().value() : null,
        user.getAvatarUrl() != null ? user.getAvatarUrl().value() : null,
        user.isActive(),
        user.getLastLoginAt(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public UserAuthDto toAuthDto(User user) {
    if (user == null) return null;
    return new UserAuthDto(
        user.getId(),
        user.getPasswordHash() != null ? user.getPasswordHash().value() : null,
        user.getLockedUntil(),
        user.isActive());
  }
}
