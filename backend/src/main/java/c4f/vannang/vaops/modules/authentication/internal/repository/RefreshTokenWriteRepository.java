package c4f.vannang.vaops.modules.authentication.internal.repository;

import c4f.vannang.vaops.modules.authentication.internal.domain.RefreshToken;
import c4f.vannang.vaops.shared.repository.BaseWriteRepository;
import java.util.UUID;

public interface RefreshTokenWriteRepository extends BaseWriteRepository<RefreshToken, UUID> {}
