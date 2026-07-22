package c4f.vannang.vaops.modules.identity.internal;

import c4f.vannang.vaops.modules.identity.api.dto.ChangePasswordRequest;
import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByAccountNameQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.SoftDeleteUserRequest;
import c4f.vannang.vaops.modules.identity.api.dto.ToggleUserStatusRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UpdateProfileRequest;
import c4f.vannang.vaops.modules.identity.internal.dto.ChangePasswordCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.CheckAvailableUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByAccountNameCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.FindByIdCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordFailedLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RecordSuccessfulLoginCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.SoftDeleteUserCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.ToggleUserStatusCommand;
import c4f.vannang.vaops.modules.identity.internal.dto.UpdateProfileCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IdentityMapper {

  RegisterCommand toInternal(RegisterRequest dto);

  FindByIdCommand toInternal(FindByIdQuery query);

  FindByAccountNameCommand toInternal(FindByAccountNameQuery query);

  FindByAccountNameCommand toInternal(FindForAuthQuery query);

  CheckAvailableUserCommand toInternal(CheckAvailableUserQuery query);

  RecordFailedLoginCommand toInternal(RecordFailedLoginRequest command);

  RecordSuccessfulLoginCommand toInternal(RecordSuccessfulLoginRequest command);

  SoftDeleteUserCommand toInternal(SoftDeleteUserRequest command);

  ToggleUserStatusCommand toInternal(ToggleUserStatusRequest command);

  UpdateProfileCommand toInternal(UpdateProfileRequest command);

  ChangePasswordCommand toInternal(ChangePasswordRequest command);
}
