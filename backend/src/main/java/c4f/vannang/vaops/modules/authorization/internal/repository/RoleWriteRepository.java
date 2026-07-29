package c4f.vannang.vaops.modules.authorization.internal.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;

public interface RoleWriteRepository extends JpaRepository<Role, UUID> {
}
