package c4f.vannang.vaops.modules.identity.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import c4f.vannang.vaops.modules.identity.api.dto.UserDTO;
import c4f.vannang.vaops.modules.identity.internal.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(source = "active", target = "active")
  UserDTO toDTO(User user);

  @Mapping(source = "active", target = "active")
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "failedLoginCount", ignore = true)
  @Mapping(target = "lockedUntil", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  User toEntity(UserDTO dto);
}
