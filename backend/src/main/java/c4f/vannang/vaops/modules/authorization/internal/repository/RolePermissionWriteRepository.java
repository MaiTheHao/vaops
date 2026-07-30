package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.RolePermission;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.RolePermissionId;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePermissionWriteRepository extends JpaRepository<RolePermission, RolePermissionId> {

  @Modifying
  @Query("DELETE FROM RolePermission rp WHERE rp.id.roleId = :roleId AND rp.id.permissionId IN :permissionIds")
  void deleteByRoleIdAndPermissionIdIn(@Param("roleId") UUID roleId, @Param("permissionIds") Collection<UUID> permissionIds);
}
