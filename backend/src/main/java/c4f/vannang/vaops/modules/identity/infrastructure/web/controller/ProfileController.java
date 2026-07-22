package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.PutUpdateProfileWebRequest;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.GetProfileUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final SoftDeleteUseCase softDeleteUseCase;

    @GetMapping
    public ResponseEntity<ProfileWebResponse> getMyProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        User user = getProfileUseCase.execute(new FindByIdCommand(principal.userId()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    public ResponseEntity<ProfileWebResponse> putUpdateProfile(
            @Valid @RequestBody PutUpdateProfileWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        User user = updateProfileUseCase.execute(
            new UpdateProfileCommand(principal.userId(), request.displayName(), request.avatarUrl()));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordWebRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        changePasswordUseCase.execute(
            new ChangePasswordCommand(principal.userId(), request.oldPassword(), request.newPassword()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        softDeleteUseCase.execute(new SoftDeleteUserCommand(principal.userId(), principal.userId()));
        return ResponseEntity.noContent().build();
    }

    private ProfileWebResponse toResponse(User user) {
        return new ProfileWebResponse(
            user.getId(),
            user.getAccountName() != null ? user.getAccountName().value() : null,
            user.getDisplayName() != null ? user.getDisplayName().value() : null,
            user.getAvatarUrl() != null ? user.getAvatarUrl().value() : null,
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
