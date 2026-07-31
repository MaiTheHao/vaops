package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.shared.repository.BaseSoftDeletableQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleQueryRepository extends BaseSoftDeletableQueryRepository<Role, UUID> {

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
  Optional<Role> findByCode(@Param("code") RoleCode code);

  @Query("SELECT r FROM Role r WHERE r.code IN :codes AND r.deletedAt IS NULL")
  List<Role> findAllByCodeIn(@Param("codes") List<RoleCode> codes);

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Role r WHERE r.code = :code AND r.deletedAt IS NULL")
  boolean existsByCode(@Param("code") RoleCode code);

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.active = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveByCode(@Param("code") RoleCode code);

  @Query(
      "SELECT DISTINCT r FROM UserRole ur JOIN Role r ON ur.id.roleId = r.id WHERE ur.id.userId = :userId"
          + " AND r.active = true AND r.deletedAt IS NULL")
  List<Role> findAllActiveByUserId(@Param("userId") UUID userId);

  @Query(
      "SELECT DISTINCT r.id FROM UserRole ur JOIN Role r ON ur.id.roleId = r.id WHERE ur.id.userId = :userId"
          + " AND r.active = true AND r.deletedAt IS NULL")
  List<UUID> findAllActiveRoleIdsByUserId(@Param("userId") UUID userId);
}
