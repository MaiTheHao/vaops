package c4f.vannang.vaops.modules.identity.infrastructure.web.mapper;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UserWebResponseDto;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.shared.dto.PageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface IdentityWebMapper {

  @Mapping(target = "accountName", expression = "java(user.getAccountName() == null ? null : user.getAccountName().value())")
  @Mapping(target = "displayName", expression = "java(user.getDisplayName() == null ? null : user.getDisplayName().value())")
  @Mapping(target = "avatarUrl", expression = "java(user.getAvatarUrl() == null ? null : user.getAvatarUrl().value())")
  UserWebResponseDto toUserWebResponseDto(User user);

  default PageResponse<UserWebResponseDto> toUserPageResponse(Page<User> page) {
    return PageResponse.from(page, this::toUserWebResponseDto);
  }
}
