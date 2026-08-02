package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignRolesToUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokeRoleFromUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
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
public class UserRoleController {

  private final UserRoleService userRoleService;
  private final AuthorizationWebMapper mapper;

  @PostMapping
  @PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> assignRoles(
      @PathVariable UUID userId,
      @Valid @RequestBody AssignRolesToUserWebRequestDto dto) {
    userRoleService.assignRolesToUser(mapper.toAssignRolesToUserCommand(userId, dto));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  @PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> revokeRoles(
      @PathVariable UUID userId,
      @Valid @RequestBody RevokeRoleFromUserWebRequestDto dto) {
    userRoleService.unAssignRolesFromUser(mapper.toRevokeRoleFromUserCommand(userId, dto));
    return ResponseEntity.noContent().build();
  }
}