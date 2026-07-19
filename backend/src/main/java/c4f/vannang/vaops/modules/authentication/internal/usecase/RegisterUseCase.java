package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final IdentityModuleApi identityModuleApi;

    public RegisterCommandResult execute(RegisterCommand command) {
        try {
            RegisterRequest identityRegisterRequest = new RegisterRequest(command.accountName(), command.password(), command.displayName(),
                    command.avatarUrl());

            UserDto registeredUser = identityModuleApi.register(identityRegisterRequest);

            return new RegisterCommandResult(registeredUser.id(), registeredUser.accountName(),
                    registeredUser.displayName(), registeredUser.avatarUrl());
        } catch (ValidationException | ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while registering. Please try again.");
        }
    }
}
