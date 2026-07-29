package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;

public interface UserRoleQueryRepository extends Repository<UserRole, UserRoleId> {

  Optional<UserRole> findById(UserRoleId id);

  @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId")
  List<UserRole> findAllByUserId(@Param("userId") UUID userId);

  @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId AND ur.revokedAt IS NULL")
  List<UserRole> findAllActiveByUserId(@Param("userId") UUID userId);

  @Query("SELECT ur FROM UserRole ur WHERE ur.id.roleId = :roleId AND ur.revokedAt IS NULL")
  List<UserRole> findAllActiveByRoleId(@Param("roleId") UUID roleId);

  @Query("SELECT ur FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId IN :roleIds")
  List<UserRole> findAllByUserIdAndRoleIdIn(@Param("userId") UUID userId, @Param("roleIds") List<UUID> roleIds);

  @Query("SELECT CASE WHEN EXISTS (" +
         "SELECT 1 FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId = :roleId AND ur.revokedAt IS NULL) THEN true ELSE false END")
  boolean existsActiveByUserIdAndRoleId(@Param("userId") UUID userId, @Param("roleId") UUID roleId);
}
