package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.shared.repository.BaseSoftDeletableQueryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PermissionQueryRepository extends BaseSoftDeletableQueryRepository<Permission, UUID> {

  @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
  Optional<Permission> findByResourceAndAction(
      @Param("resource") PermissionResource resource, @Param("action") PermissionAction action);

  @Query("SELECT p FROM Permission p WHERE p.resource IN :resources AND p.deletedAt IS NULL")
  List<Permission> findAllByResourceIn(@Param("resources") List<PermissionResource> resources);

  @Query("SELECT p FROM Permission p WHERE p.action IN :actions AND p.deletedAt IS NULL")
  List<Permission> findAllByActionIn(@Param("actions") List<PermissionAction> actions);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.resource = :resource AND p.deletedAt IS NULL")
  boolean existsByResource(@Param("resource") PermissionResource resource);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.action = :action AND p.deletedAt IS NULL")
  boolean existsByAction(@Param("action") PermissionAction action);

  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.deletedAt IS NULL")
  boolean existsByResourceAndAction(
      @Param("resource") PermissionResource resource, @Param("action") PermissionAction action);

  @Query(
      "SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action AND p.active"
          + " = true AND p.deletedAt IS NULL")
  Optional<Permission> findActiveByResourceAndAction(
      @Param("resource") PermissionResource resource, @Param("action") PermissionAction action);

  @Query("SELECT DISTINCT p FROM UserRole ur "
      + "JOIN Role r ON ur.id.roleId = r.id "
      + "JOIN RolePermission rp ON r.id = rp.id.roleId "
      + "JOIN Permission p ON rp.id.permissionId = p.id "
      + "WHERE ur.id.userId = :userId "
      + "AND r.active = true AND r.deletedAt IS NULL "
      + "AND p.active = true AND p.deletedAt IS NULL")
  List<Permission> findAllActiveByUserId(@Param("userId") UUID userId);

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
      @Param("resource") PermissionResource resource,
      @Param("action") PermissionAction action);
}
