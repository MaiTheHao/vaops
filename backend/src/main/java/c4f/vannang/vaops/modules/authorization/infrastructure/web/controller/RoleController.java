package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignPermissionsRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokePermissionsRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.UpdateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.RoleWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.dto.RoleSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.service.RoleService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role CRUD, permission assignment/revocation, and search operations")
public class RoleController {

  private final RoleService roleService;
  private final AuthorizationWebMapper mapper;

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE:CREATE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new role")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Role created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload or duplicate role code"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<RoleWebResponseDto> createRole(
      @Valid @RequestBody CreateRoleWebRequestDto dto) {
    RoleWebResponseDto response = mapper.toRoleWebResponseDto(
        roleService.createRole(mapper.toCreateRoleCommand(dto)));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{roleId}")
  @PreAuthorize("hasAuthority('ROLE:UPDATE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update role details by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Role updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Role not found")
  })
  public ResponseEntity<RoleWebResponseDto> updateRole(
      @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID roleId,
      @Valid @RequestBody UpdateRoleWebRequestDto dto) {
    RoleWebResponseDto response = mapper.toRoleWebResponseDto(
        roleService.updateRole(mapper.toUpdateRoleCommand(roleId, dto)));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{roleId}")
  @PreAuthorize("hasAuthority('ROLE:DELETE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Soft delete role by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Role soft deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Role not found")
  })
  public ResponseEntity<Void> deleteRole(
      @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID roleId,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    roleService.softDeleteRole(roleId, principal != null ? principal.userId() : null);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{roleId}")
  @PreAuthorize("hasAuthority('ROLE:READ') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get role details with assigned permissions by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Role details retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Role not found")
  })
  public ResponseEntity<RoleWebResponseDto> getRole(
      @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID roleId) {
    RoleWebResponseDto response = mapper.toRoleWebResponseDto(roleService.getRoleById(roleId));
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE:READ') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Search roles with pagination and filters")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Paginated roles search results"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PageResponse<RoleWebResponseDto>> searchRoles(
      @Parameter(name = "keyword", description = "Search keyword") @RequestParam(name = "keyword", required = false) String keyword,
      @Parameter(name = "code", description = "Filter by role code") @RequestParam(name = "code", required = false) String code,
      @Parameter(name = "isActive", description = "Filter by active status") @RequestParam(name = "isActive", required = false) Boolean isActive,
      @Parameter(name = "userId", description = "Filter by assigned user ID") @RequestParam(name = "userId", required = false) UUID userId,
      @Parameter(name = "createdFrom", description = "Filter created from timestamp") @RequestParam(name = "createdFrom", required = false) Instant createdFrom,
      @Parameter(name = "createdTo", description = "Filter created to timestamp") @RequestParam(name = "createdTo", required = false) Instant createdTo,
      @Parameter(name = "page", description = "Page index (0-based)") @RequestParam(name = "page", defaultValue = "0") int page,
      @Parameter(name = "size", description = "Page size limit") @RequestParam(name = "size", defaultValue = "20") int size,
      @Parameter(name = "sortBy", description = "Sort field") @RequestParam(name = "sortBy", required = false) String sortBy,
      @Parameter(name = "sortDirection", description = "Sort direction (ASC or DESC)") @RequestParam(name = "sortDirection", required = false) String sortDirection) {
    RoleSearchCriteria criteria = new RoleSearchCriteria(
        keyword, code, isActive, userId, createdFrom, createdTo, page, size, sortBy, sortDirection);
    PageResponse<RoleWebResponseDto> response = mapper.toRolePageResponse(
        roleService.searchRoles(criteria));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{roleId}/permissions")
  @PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Assign permissions to role")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Permissions assigned to role successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid permission ID payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Role not found")
  })
  public ResponseEntity<Void> assignPermissions(
      @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID roleId,
      @Valid @RequestBody AssignPermissionsRequestDto dto) {
    roleService.assignPermissionsToRole(mapper.toAssignPermissionsToRoleCommand(roleId, dto));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{roleId}/permissions")
  @PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Revoke permissions from role")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Permissions revoked from role successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid permission ID payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Role not found")
  })
  public ResponseEntity<Void> revokePermissions(
      @Parameter(description = "Role UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID roleId,
      @Valid @RequestBody RevokePermissionsRequestDto dto) {
    roleService.unassignPermissionsFromRole(mapper.toRevokePermissionFromRoleCommand(roleId, dto));
    return ResponseEntity.noContent().build();
  }
}