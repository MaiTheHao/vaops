package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface RoleQueryRepository extends BaseQueryRepository<Role, UUID> {

  Optional<Role> findById(UUID id);

  @Query("SELECT r FROM Role r WHERE r.id = :id AND r.isActive = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveById(@Param("id") UUID id);

  Optional<Role> findByCode(String code);

  boolean existsByCode(String code);

  @Query("SELECT r FROM Role r WHERE r.code = :code AND r.isActive = true AND r.deletedAt IS NULL")
  Optional<Role> findActiveByCode(@Param("code") String code);

  List<Role> findAllByIdIn(List<UUID> ids);

  @Query("SELECT r FROM Role r WHERE r.id IN :ids AND r.isActive = true AND r.deletedAt IS NULL")
  List<Role> findAllActiveByIds(@Param("ids") List<UUID> ids);
}
