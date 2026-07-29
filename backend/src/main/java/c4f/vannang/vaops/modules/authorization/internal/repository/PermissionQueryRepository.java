package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;

public interface PermissionQueryRepository extends Repository<Permission, UUID> {

  Optional<Permission> findById(UUID id);

  @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.isActive = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveById(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
  Optional<Permission> findByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  boolean existsByResourceAndAction(String resource, String action);

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.isActive = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  List<Permission> findAllByIdIn(List<UUID> ids);

  @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.isActive = true AND p.deletedAt IS NULL")
  List<Permission> findAllActiveByIds(@Param("ids") List<UUID> ids);

  @Query("SELECT p FROM Permission p WHERE p.isActive = true AND p.deletedAt IS NULL ORDER BY p.resource ASC, p.action ASC")
  List<Permission> findAllActive();

  @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId AND r.isActive = true AND r.deletedAt IS NULL AND p.isActive = true AND p.deletedAt IS NULL")
  List<Permission> findActivePermissionsByRoleId(@Param("roleId") UUID roleId);

  @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN UserRole ur ON ur.id.roleId = r.id WHERE ur.id.userId = :userId AND ur.revokedAt IS NULL AND r.isActive = true AND r.deletedAt IS NULL AND p.isActive = true AND p.deletedAt IS NULL")
  List<Permission> findActivePermissionsByUserId(@Param("userId") UUID userId);

  @Query("SELECT CASE WHEN EXISTS (" +
         "SELECT 1 FROM Permission p JOIN p.roles r JOIN UserRole ur ON ur.id.roleId = r.id " +
         "WHERE ur.id.userId = :userId AND p.resource = :resource AND p.action = :action " +
         "AND ur.revokedAt IS NULL AND r.isActive = true AND r.deletedAt IS NULL " +
         "AND p.isActive = true AND p.deletedAt IS NULL) THEN true ELSE false END")
  boolean hasPermission(@Param("userId") UUID userId, @Param("resource") String resource, @Param("action") String action);
}
