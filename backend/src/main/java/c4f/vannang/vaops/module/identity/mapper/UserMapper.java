package c4f.vannang.vaops.module.identity.mapper;

import c4f.vannang.vaops.module.identity.domain.User;
import c4f.vannang.vaops.module.identity.dto.CreateUserDto;
import c4f.vannang.vaops.module.identity.dto.PartialUpdateUserDto;
import c4f.vannang.vaops.module.identity.dto.SafeUserDto;
import c4f.vannang.vaops.module.identity.dto.UpdateUserDto;
import org.mapstruct.*;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "isActive", source = "active")
    SafeUserDto toSafeUserDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "identities", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    User toUser(CreateUserDto createUserDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "identities", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    void updateUserFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "identities", ignore = true)
    @Mapping(target = "refreshTokens", ignore = true)
    void partialUpdateUserFromDto(PartialUpdateUserDto partialUpdateUserDto, @MappingTarget User user);
}
