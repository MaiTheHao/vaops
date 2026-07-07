package c4f.vannang.vaops.modules.identity.internal.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.Repository;
import c4f.vannang.vaops.modules.identity.internal.domain.User;

public interface UserQueryRepository extends Repository<User, UUID> {

  Optional<User> findByIdAndDeletedAtIsNull(UUID id);

  Optional<User> findByAccountNameAndDeletedAtIsNull(String accountName);

  boolean existsByAccountNameAndDeletedAtIsNull(String accountName);
}
