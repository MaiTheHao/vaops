package c4f.vannang.vaops.modules.authentication.internal.repository;

import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.shared.repository.BaseQueryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenQueryRepository extends BaseQueryRepository<RefreshToken, UUID> {

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash")
  Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

  @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revokedAt IS NULL AND rt.expiredAt > CURRENT_TIMESTAMP")
  List<RefreshToken> findValidRefreshTokensByUserId(@Param("userId") UUID userId);

}
