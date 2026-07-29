package c4f.vannang.vaops.modules.identity.internal.repository;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

public interface UserQueryRepository extends BaseQueryRepository<User, UUID> {

  Optional<User> findById(UUID id);

  @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true AND u.deletedAt IS NULL")
  Optional<User> findActiveById(@Param("id") UUID id);

  List<User> findAllByIdIn(List<UUID> ids);

  @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.isActive = true AND u.deletedAt IS NULL")
  List<User> findAllActiveByIds(@Param("ids") List<UUID> ids);

  @Query("SELECT u FROM User u WHERE u.accountName = :accountName")
  Optional<User> findByAccountName(@Param("accountName") AccountName accountName);

  @Query("SELECT u FROM User u WHERE u.accountName = :accountName AND u.isActive = true AND u.deletedAt IS NULL")
  Optional<User> findActiveByAccountName(@Param("accountName") AccountName accountName);

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.accountName = :accountName AND u.isActive = true AND u.deletedAt IS NULL")
  boolean existsActiveByAccountName(@Param("accountName") AccountName accountName);

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.accountName = :accountName")
  boolean existsByAccountName(@Param("accountName") AccountName accountName);
}
