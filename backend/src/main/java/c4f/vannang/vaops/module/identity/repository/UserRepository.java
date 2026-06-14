package c4f.vannang.vaops.module.identity.repository;

import c4f.vannang.vaops.module.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  Optional<User> findByPhone(String phone);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);
}
