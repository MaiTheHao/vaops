package c4f.vannang.vaops.modules.authorization.internal.repository;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.shared.repository.BaseWriteRepository;
import java.util.UUID;

public interface PermissionWriteRepository extends BaseWriteRepository<Permission, UUID> {
}
