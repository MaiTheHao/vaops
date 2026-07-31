package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;
import c4f.vannang.vaops.shared.repository.BaseWriteRepository;

public interface UserRoleWriteRepository extends BaseWriteRepository<UserRole, UserRoleId> {

  @Modifying
  @Query("DELETE FROM UserRole ur WHERE ur.id.userId = :userId AND ur.id.roleId IN :roleIds")
  void deleteByUserIdAndRoleIdIn(@Param("userId") UUID userId, @Param("roleIds") Collection<UUID> roleIds);
}
