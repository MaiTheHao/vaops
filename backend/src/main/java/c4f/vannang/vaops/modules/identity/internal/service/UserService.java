package c4f.vannang.vaops.modules.identity.internal.service;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.dto.*;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface UserService {
  User register(RegisterCommand dto);
  Page<User> searchUsers(UserSearchCriteria criteria);
  Optional<User> findUserById(FindByIdCommand command);
  Optional<User> findUserByAccountName(FindByAccountNameCommand command);
  void checkAvailableUser(CheckAvailableUserCommand command);
  void softDelete(SoftDeleteUserCommand command);
  void softDeleteUser(UUID userId, UUID deletedBy);
  void hardDeleteUser(UUID userId);
  void toggleStatus(ToggleUserStatusCommand command);
  void recordSuccessfulLogin(RecordSuccessfulLoginCommand command);
  void recordFailedLogin(RecordFailedLoginCommand command);
}
