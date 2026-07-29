package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;

public interface UserRoleWriteRepository extends JpaRepository<UserRole, UserRoleId> {
  void deleteByUserIdAndRoleIdIn(UUID userId, Collection<UUID> roleIds);
}
