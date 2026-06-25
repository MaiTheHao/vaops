package c4f.vannang.vaops.modules.identity.internal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import c4f.vannang.vaops.modules.identity.internal.domain.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
