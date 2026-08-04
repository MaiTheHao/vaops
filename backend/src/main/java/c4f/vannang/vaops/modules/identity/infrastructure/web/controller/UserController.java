package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ToggleUserStatusWebRequestDto;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UserWebResponseDto;
import c4f.vannang.vaops.modules.identity.infrastructure.web.mapper.IdentityWebMapper;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UserSearchCriteria;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final IdentityWebMapper mapper;

  @DeleteMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER:DELETE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> deleteUser(
      @PathVariable UUID userId,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    userService.softDeleteUser(userId, principal != null ? principal.userId() : null);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER:READ') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<UserWebResponseDto> getUser(@PathVariable UUID userId) {
    User user = userService.findUserById(new FindByIdCommand(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return ResponseEntity.ok(mapper.toUserWebResponseDto(user));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('USER:READ') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<PageResponse<UserWebResponseDto>> searchUsers(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDirection) {
    UserSearchCriteria criteria = new UserSearchCriteria(
        page,
        size,
        sortBy,
        "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
        keyword,
        isActive);
    Page<User> users = userService.searchUsers(criteria);
    return ResponseEntity.ok(mapper.toUserPageResponse(users));
  }

  @PatchMapping("/{userId}/status")
  @PreAuthorize("hasAuthority('USER:UPDATE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> toggleUserStatus(
      @PathVariable UUID userId,
      @Valid @RequestBody ToggleUserStatusWebRequestDto request) {
    userService.toggleStatus(new ToggleUserStatusCommand(userId, request.active()));
    return ResponseEntity.noContent().build();
  }
}
