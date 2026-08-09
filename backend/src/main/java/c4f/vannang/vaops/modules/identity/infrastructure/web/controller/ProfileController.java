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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management operations")
public class ProfileController {

    private final IdentityProfileAPIService identityProfileService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROFILE:READ')")
    @Operation(summary = "Get current authenticated user profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ProfileWebResponse> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserDto user = identityProfileService.getProfile(new FindByIdQuery(principal.userId()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PROFILE:UPDATE')")
    @Operation(summary = "Update current user profile information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ProfileWebResponse> putUpdateProfile(
            @Valid @RequestBody PutUpdateProfileWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserDto user = identityProfileService.updateProfile(
            new UpdateProfileRequest(principal.userId(), request.displayName(), request.avatarUrl()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    @PreAuthorize("hasAuthority('PROFILE:UPDATE')")
    @Operation(summary = "Change user account password")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password data or current password incorrect"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        identityProfileService.changePassword(
            new ChangePasswordRequest(principal.userId(), request.oldPassword(), request.newPassword()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('PROFILE:DELETE')")
    @Operation(summary = "Delete current user account (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {

        userService.softDeleteUser(principal.userId(), principal.userId());
        
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

