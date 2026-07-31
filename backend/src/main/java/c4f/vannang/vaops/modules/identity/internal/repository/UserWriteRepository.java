package c4f.vannang.vaops.modules.identity.internal.repository;

import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.shared.repository.BaseWriteRepository;
import java.util.UUID;

public interface UserWriteRepository extends BaseWriteRepository<User, UUID> {
}
