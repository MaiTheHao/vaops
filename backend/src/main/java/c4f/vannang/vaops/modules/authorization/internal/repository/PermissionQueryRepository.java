package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionQueryRepository extends BaseQueryRepository<Permission, UUID> {

  @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<Permission> findById(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.id = :id")
  Optional<Permission> findByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.id = :id")
  boolean existsByIdWithDeleted(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.id = :id AND p.active = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveById(@Param("id") UUID id);

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
  Optional<Permission> findByResourceAndAction(
      @Param("resource") String resource, @Param("action") String action);

  @Query("SELECT p FROM Permission p WHERE p.resource IN :resources AND p.deletedAt IS NULL")
  List<Permission> findAllByResourceIn(@Param("resources") List<String> resources);

  @Query("SELECT p FROM Permission p WHERE p.action IN :actions AND p.deletedAt IS NULL")
  List<Permission> findAllByActionIn(@Param("actions") List<String> actions);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.resource = :resource AND p.deletedAt IS NULL")
  boolean existsByResource(@Param("resource") String resource);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.action = :action AND p.deletedAt IS NULL")
  boolean existsByAction(@Param("action") String action);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
  boolean existsByResourceAndAction(@Param("resource") String resource, @Param("action") String action);

  @Query(
      "SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.active"
          + " = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveByResourceAndAction(
      @Param("resource") String resource, @Param("action") String action);

  @Query("SELECT p FROM Permission p WHERE p.id IN :ids AND p.deletedAt IS NULL")
  List<Permission> findAllByIdIn(@Param("ids") List<UUID> ids);

  @Query(
      "SELECT p FROM Permission p WHERE p.id IN :ids AND p.active = true AND p.deletedAt IS NULL")
  List<Permission> findAllActiveByIdIn(@Param("ids") List<UUID> ids);

  @Query("SELECT DISTINCT p FROM UserRole ur "
      + "JOIN Role r ON ur.id.roleId = r.id "
      + "JOIN RolePermission rp ON r.id = rp.id.roleId "
      + "JOIN Permission p ON rp.id.permissionId = p.id "
      + "WHERE ur.id.userId = :userId "
      + "AND r.active = true AND r.deletedAt IS NULL "
      + "AND p.active = true AND p.deletedAt IS NULL")
  List<Permission> findActiveByUserId(@Param("userId") UUID userId);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END "
      + "FROM UserRole ur "
      + "JOIN Role r ON ur.id.roleId = r.id "
      + "JOIN RolePermission rp ON r.id = rp.id.roleId "
      + "JOIN Permission p ON rp.id.permissionId = p.id "
      + "WHERE ur.id.userId = :userId "
      + "AND p.resource = :resource AND p.action = :action "
      + "AND r.active = true AND r.deletedAt IS NULL "
      + "AND p.active = true AND p.deletedAt IS NULL")
  boolean existsActiveByUserIdAndResourceAndAction(
      @Param("userId") UUID userId,
      @Param("resource") String resource,
      @Param("action") String action);
}
