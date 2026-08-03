package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityProfileAPIService;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.PutUpdateProfileWebRequest;
import c4f.vannang.vaops.modules.identity.internal.service.UserService;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final IdentityProfileAPIService identityProfileService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROFILE:READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProfileWebResponse> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserDto user = identityProfileService.getProfile(new FindByIdQuery(principal.userId()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PROFILE:UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProfileWebResponse> putUpdateProfile(
            @Valid @RequestBody PutUpdateProfileWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserDto user = identityProfileService.updateProfile(
            new UpdateProfileRequest(principal.userId(), request.displayName(), request.avatarUrl()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    @PreAuthorize("hasAuthority('PROFILE:UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        identityProfileService.changePassword(
            new ChangePasswordRequest(principal.userId(), request.oldPassword(), request.newPassword()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @RequestParam(defaultValue = "false") boolean hard,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (hard) {
            userService.hardDeleteUser(principal.userId());
        } else {
            userService.softDeleteUser(principal.userId(), principal.userId());
        }
        return ResponseEntity.noContent().build();
    }

    private ProfileWebResponse toResponse(UserDto user) {
        return new ProfileWebResponse(
            user.id(),
            user.accountName(),
            user.displayName(),
            user.avatarUrl(),
            user.lastLoginAt(),
            user.createdAt(),
            user.updatedAt()
        );
    }
}
