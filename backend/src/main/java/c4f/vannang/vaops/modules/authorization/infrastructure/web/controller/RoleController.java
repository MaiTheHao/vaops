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
public class RoleController {

  private final RoleService roleService;
  private final AuthorizationWebMapper mapper;

  @PostMapping
  @PreAuthorize("hasAuthority('ROLE:CREATE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<RoleWebResponseDto> createRole(
      @Valid @RequestBody CreateRoleWebRequestDto dto) {
    RoleWebResponseDto response = mapper.toRoleWebResponseDto(
        roleService.createRole(mapper.toCreateRoleCommand(dto)));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{roleId}")
  @PreAuthorize("hasAuthority('ROLE:UPDATE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<RoleWebResponseDto> updateRole(
      @PathVariable UUID roleId,
      @Valid @RequestBody UpdateRoleWebRequestDto dto) {
    RoleWebResponseDto response = mapper.toRoleWebResponseDto(
        roleService.updateRole(mapper.toUpdateRoleCommand(roleId, dto)));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{roleId}")
  @PreAuthorize("hasAuthority('ROLE:DELETE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> deleteRole(
      @PathVariable UUID roleId,
      @RequestParam(defaultValue = "false") boolean hard,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    if (hard) {
      roleService.hardDeleteRole(roleId);
    } else {
      roleService.softDeleteRole(roleId, principal != null ? principal.userId() : null);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{roleId}")
  @PreAuthorize("hasAuthority('ROLE:READ') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<RoleWebResponseDto> getRole(@PathVariable UUID roleId) {
    RoleWebResponseDto response = mapper.toRoleWebResponseDto(roleService.getRoleById(roleId));
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ROLE:READ') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<PageResponse<RoleWebResponseDto>> searchRoles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) Instant createdFrom,
      @RequestParam(required = false) Instant createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDirection) {
    RoleSearchCriteria criteria = new RoleSearchCriteria(
        keyword, code, isActive, userId, createdFrom, createdTo, page, size, sortBy, sortDirection);
    PageResponse<RoleWebResponseDto> response = mapper.toRolePageResponse(
        roleService.searchRoles(criteria));
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{roleId}/permissions")
  @PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> assignPermissions(
      @PathVariable UUID roleId,
      @Valid @RequestBody AssignPermissionsRequestDto dto) {
    roleService.assignPermissionsToRole(mapper.toAssignPermissionsToRoleCommand(roleId, dto));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{roleId}/permissions")
  @PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> revokePermissions(
      @PathVariable UUID roleId,
      @Valid @RequestBody RevokePermissionsRequestDto dto) {
    roleService.unassignPermissionsFromRole(mapper.toRevokePermissionFromRoleCommand(roleId, dto));
    return ResponseEntity.noContent().build();
  }
}