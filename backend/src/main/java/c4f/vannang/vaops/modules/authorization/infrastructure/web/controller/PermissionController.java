package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.UpdatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.PermissionWebResponseDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.service.PermissionService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class PermissionController {

  private final PermissionService permissionService;
  private final AuthorizationWebMapper mapper;

  @PostMapping
  public ResponseEntity<PermissionWebResponseDto> createPermission(
      @Valid @RequestBody CreatePermissionWebRequestDto dto) {
    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(
        permissionService.createPermission(mapper.toCreatePermissionCommand(dto)));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{permissionId}")
  public ResponseEntity<PermissionWebResponseDto> updatePermission(
      @PathVariable UUID permissionId,
      @Valid @RequestBody UpdatePermissionWebRequestDto dto) {
    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(
        permissionService.updatePermission(mapper.toUpdatePermissionCommand(permissionId, dto)));
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{permissionId}")
  public ResponseEntity<Void> deletePermission(
      @PathVariable UUID permissionId,
      @RequestParam(defaultValue = "false") boolean hard,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    if (hard) {
      permissionService.hardDeletePermission(permissionId);
    } else {
      permissionService.softDeletePermission(permissionId, principal != null ? principal.userId() : null);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{permissionId}")
  public ResponseEntity<PermissionWebResponseDto> getPermission(@PathVariable UUID permissionId) {
    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(
        permissionService.getPermissionById(permissionId));
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<PageResponse<PermissionWebResponseDto>> searchPermissions(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String resource,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) Collection<UUID> roleIds,
      @RequestParam(required = false) Instant createdFrom,
      @RequestParam(required = false) Instant createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) String sortDirection) {
    PermissionSearchCriteria criteria = new PermissionSearchCriteria(
        keyword, resource, action, isActive, roleIds, createdFrom, createdTo, page, size, sortBy, sortDirection);
    PageResponse<PermissionWebResponseDto> response = mapper.toPermissionPageResponse(
        permissionService.searchPermissions(criteria));
    return ResponseEntity.ok(response);
  }
}