package c4f.vannang.vaops.modules.authentication.internal.repository;

import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenWriteRepository extends JpaRepository<RefreshToken, UUID> {}
