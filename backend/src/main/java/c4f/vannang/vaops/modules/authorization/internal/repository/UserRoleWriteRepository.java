package c4f.vannang.vaops.modules.authorization.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import c4f.vannang.vaops.modules.authorization.internal.domain.UserRole;
import c4f.vannang.vaops.modules.authorization.internal.domain.id.UserRoleId;

public interface UserRoleWriteRepository extends JpaRepository<UserRole, UserRoleId> {
}
