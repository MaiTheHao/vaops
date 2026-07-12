package c4f.vannang.vaops.modules.authentication.internal.usecase;

import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterDto;
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

    public RegisterCommandResultDto execute(RegisterCommandDto dto) {
        try {
            RegisterDto identityRegisterDto = new RegisterDto(dto.accountName(), dto.password(), dto.displayName(),
                    dto.avatarUrl());

            UserDto registeredUser = identityModuleApi.register(identityRegisterDto);

            return new RegisterCommandResultDto(registeredUser.id(), registeredUser.accountName(),
                    registeredUser.displayName(), registeredUser.avatarUrl());
        } catch (ValidationException | ResourceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException("Unexpected error while registering. Please try again.");
        }
    }
}
