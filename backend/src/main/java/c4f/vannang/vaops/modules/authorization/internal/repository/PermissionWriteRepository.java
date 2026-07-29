package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;

public interface PermissionWriteRepository extends JpaRepository<Permission, UUID> {
}
