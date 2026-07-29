package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface UserRoleQueryRepository extends BaseQueryRepository<UserRole, UserRoleId> {

  Optional<UserRole> findById(UserRoleId id);

  @Query("SELECT CASE WHEN EXISTS (" +
         "SELECT 1 FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId = :roleId AND ur.revokedAt IS NULL) THEN true ELSE false END")
  boolean existsActiveByUserIdAndRoleId(@Param("userId") UUID userId, @Param("roleId") UUID roleId);
}
