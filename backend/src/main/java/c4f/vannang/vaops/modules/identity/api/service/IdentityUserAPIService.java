package c4f.vannang.vaops.modules.identity.api.service;

import java.util.Optional;

import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindByIdQuery;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.RecordFailedLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RecordSuccessfulLoginRequest;
import c4f.vannang.vaops.modules.identity.api.dto.RegisterRequest;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.dto.UserDto;

public interface IdentityUserAPIService {

  Optional<UserAuthDto> getUserForAuth(FindForAuthQuery query);

  void checkAvailableUser(CheckAvailableUserQuery query);

  void recordSuccessfulLogin(RecordSuccessfulLoginRequest command);

  void recordFailedLogin(RecordFailedLoginRequest command);

  UserDto register(RegisterRequest registerDto);

  Optional<UserDto> getUserById(FindByIdQuery query);
}
