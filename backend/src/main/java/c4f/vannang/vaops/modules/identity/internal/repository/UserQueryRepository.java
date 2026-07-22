package c4f.vannang.vaops.modules.identity.internal.repository;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface UserQueryRepository extends Repository<User, UUID> {

  @Query("SELECT u FROM User u WHERE u.id = :id")
  Optional<User> findById(@Param("id") UUID id);

  @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
  Optional<User> findActiveById(@Param("id") UUID id);

  @Query("SELECT u FROM User u WHERE u.accountName = :accountName AND u.deletedAt IS NULL")
  Optional<User> findActiveByAccountName(@Param("accountName") AccountName accountName);

  @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.accountName ="
      + " :accountName AND u.deletedAt IS NULL")
  boolean existsActiveByAccountName(@Param("accountName") AccountName accountName);
}
