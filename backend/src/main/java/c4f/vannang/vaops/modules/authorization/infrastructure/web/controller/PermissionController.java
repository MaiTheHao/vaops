package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.UpdatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.PermissionWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.service.PermissionService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Collection;
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
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "Permission CRUD and search operations")
public class PermissionController {

  private final PermissionService permissionService;
  private final AuthorizationWebMapper mapper;

  @PostMapping
  @PreAuthorize("hasAuthority('PERMISSION:CREATE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new permission")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Permission created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload or duplicate permission"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PermissionWebResponseDto> createPermission(
      @Valid @RequestBody CreatePermissionWebRequestDto dto) {
    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(
        permissionService.createPermission(mapper.toCreatePermissionCommand(dto)));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{permissionId}")
  @PreAuthorize("hasAuthority('PERMISSION:UPDATE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update permission details by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Permission updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Permission not found")
  })
  public ResponseEntity<PermissionWebResponseDto> updatePermission(
      @Parameter(description = "Permission UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID permissionId,
      @Valid @RequestBody UpdatePermissionWebRequestDto dto) {
    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(
        permissionService.updatePermission(mapper.toUpdatePermissionCommand(permissionId, dto)));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{permissionId}")
  @PreAuthorize("hasAuthority('PERMISSION:DELETE') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Soft delete permission by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Permission soft deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Permission not found")
  })
  public ResponseEntity<Void> deletePermission(
      @Parameter(description = "Permission UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID permissionId,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    permissionService.softDeletePermission(permissionId, principal != null ? principal.userId() : null);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{permissionId}")
  @PreAuthorize("hasAuthority('PERMISSION:READ') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get permission details by ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Permission details retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden"),
      @ApiResponse(responseCode = "404", description = "Permission not found")
  })
  public ResponseEntity<PermissionWebResponseDto> getPermission(
      @Parameter(description = "Permission UUID", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID permissionId) {
    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(
        permissionService.getPermissionById(permissionId));
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERMISSION:READ') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Search permissions with pagination and filters")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Paginated permissions search results"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated"),
      @ApiResponse(responseCode = "403", description = "Forbidden")
  })
  public ResponseEntity<PageResponse<PermissionWebResponseDto>> searchPermissions(
      @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
      @Parameter(description = "Filter by resource") @RequestParam(required = false) String resource,
      @Parameter(description = "Filter by action") @RequestParam(required = false) String action,
      @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
      @Parameter(description = "Filter by assigned role IDs") @RequestParam(required = false) Collection<UUID> roleIds,
      @Parameter(description = "Filter created from timestamp") @RequestParam(required = false) Instant createdFrom,
      @Parameter(description = "Filter created to timestamp") @RequestParam(required = false) Instant createdTo,
      @Parameter(description = "Page index (0-based)") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size limit") @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field") @RequestParam(required = false) String sortBy,
      @Parameter(description = "Sort direction (ASC or DESC)") @RequestParam(required = false) String sortDirection) {
    PermissionSearchCriteria criteria = new PermissionSearchCriteria(
        keyword, resource, action, isActive, roleIds, createdFrom, createdTo, page, size, sortBy, sortDirection);
    PageResponse<PermissionWebResponseDto> response = mapper.toPermissionPageResponse(
        permissionService.searchPermissions(criteria));
    return ResponseEntity.ok(response);
  }
}