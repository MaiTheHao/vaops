package c4f.vannang.vaops.modules.identity.internal.mapper;

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
        user.getAccountName(),
        user.getDisplayName(),
        user.getAvatarUrl(),
        user.isActive(),
        user.getLastLoginAt(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public UserAuthDto toAuthDto(User user) {
    if (user == null) return null;
    return new UserAuthDto(
        user.getId(),
        user.getPasswordHash(),
        user.getLockedUntil(),
        user.isActive());
  }
}
