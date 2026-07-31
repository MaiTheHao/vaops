package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.shared.repository.BaseWriteRepository;
import java.util.UUID;

public interface RoleWriteRepository extends BaseWriteRepository<Role, UUID> {
}
