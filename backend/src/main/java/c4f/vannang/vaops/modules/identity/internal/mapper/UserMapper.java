package c4f.vannang.vaops.modules.identity.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "active", target = "active")
  UserDto toDTO(User user);

  @Mapping(source = "active", target = "active")
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "failedLoginCount", ignore = true)
  @Mapping(target = "lockedUntil", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  User toEntity(UserDto dto);

  UserAuthDto toAuthDTO(User user);
}
