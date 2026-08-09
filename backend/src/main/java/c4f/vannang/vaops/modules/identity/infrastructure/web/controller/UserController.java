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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Management", description = "User searching, profile viewing, status toggle, and deletion")
public class UserController {

  private final UserService userService;
  private final IdentityWebMapper mapper;

  @DeleteMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER:DELETE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Soft delete user account by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User soft deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "Target user UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    userService.softDeleteUser(userId, principal != null ? principal.userId() : null);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{userId}")
  @PreAuthorize("hasAuthority('USER:READ') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get user profile details by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<UserWebResponseDto> getUser(
      @Parameter(description = "Target user UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId) {
    User user = userService.findUserById(new FindByIdCommand(userId))
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return ResponseEntity.ok(mapper.toUserWebResponseDto(user));
  }

  @GetMapping
  @PreAuthorize("hasAuthority('USER:READ') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Search users with pagination and filters")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Paginated users search results"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PageResponse<UserWebResponseDto>> searchUsers(
      @Parameter(description = "Search keyword (matches account name or display name)") @RequestParam(required = false) String keyword,
      @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
      @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size limit") @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
      @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(required = false) String sortDirection) {
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
  @Operation(summary = "Enable or disable user status")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User status updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  public ResponseEntity<Void> toggleUserStatus(
      @Parameter(description = "Target user UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,
      @Valid @RequestBody ToggleUserStatusWebRequestDto request) {
    userService.toggleStatus(new ToggleUserStatusCommand(userId, request.active()));
    return ResponseEntity.noContent().build();
  }
}

