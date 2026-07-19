package c4f.vannang.vaops.modules.identity.infrastructure.web.controller;

import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ChangePasswordWebRequest;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.ProfileWebResponse;
import c4f.vannang.vaops.modules.identity.infrastructure.web.dto.UpdateProfileWebRequest;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import c4f.vannang.vaops.modules.identity.internal.usecase.ChangePasswordUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.FindUserByIdUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.SoftDeleteUseCase;
import c4f.vannang.vaops.modules.identity.internal.usecase.UpdateProfileUseCase;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final FindUserByIdUseCase findUserByIdUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final SoftDeleteUseCase softDeleteUseCase;

    @GetMapping
    public ResponseEntity<ProfileWebResponse> getMyProfile(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        User user = findUserByIdUseCase.execute(new FindByIdCommand(userId))
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    public ResponseEntity<ProfileWebResponse> updateProfile(
            @Valid @RequestBody UpdateProfileWebRequest request,
            Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        updateProfileUseCase.execute(new UpdateProfileCommand(userId, request.displayName(), request.avatarUrl()));
        User user = findUserByIdUseCase.execute(new FindByIdCommand(userId))
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordWebRequest request,
            Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        changePasswordUseCase.execute(new ChangePasswordCommand(userId, request.oldPassword(), request.newPassword()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        softDeleteUseCase.execute(new SoftDeleteUserCommand(userId, userId));
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
