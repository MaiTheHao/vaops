package c4f.vannang.vaops.modules.authentication.internal.repository;

import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenQueryRepository extends Repository<RefreshToken, UUID> {

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash")
  Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId")
  Optional<RefreshToken> findByUserId(@Param("userId") UUID userId);

}
