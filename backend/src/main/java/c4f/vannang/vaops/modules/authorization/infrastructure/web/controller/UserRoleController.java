package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignRolesToUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokeRoleFromUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
@RequiredArgsConstructor
@Tag(name = "User Role Assignment", description = "User role assignment and revocation")
public class UserRoleController {

  private final UserRoleService userRoleService;
  private final AuthorizationWebMapper mapper;

  @PostMapping
  @PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Assign roles to user")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Roles assigned to user successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid role ID payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<Void> assignRoles(
      @Parameter(description = "Target user UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,
      @Valid @RequestBody AssignRolesToUserWebRequestDto dto) {
    userRoleService.assignRolesToUser(mapper.toAssignRolesToUserCommand(userId, dto));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  @PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Revoke roles from user")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Roles revoked from user successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid role ID payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<Void> revokeRoles(
      @Parameter(description = "Target user UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID userId,
      @Valid @RequestBody RevokeRoleFromUserWebRequestDto dto) {
    userRoleService.unAssignRolesFromUser(mapper.toRevokeRoleFromUserCommand(userId, dto));
    return ResponseEntity.noContent().build();
  }
}