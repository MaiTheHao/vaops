package c4f.vannang.vaops.shared.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base write repository for all entities, isolating persistence-side operations behind a single
 * shared marker interface.
 *
 * @param <T>  the entity type
 * @param <ID> the entity id type
 */
@NoRepositoryBean
public interface BaseWriteRepository<T, ID> extends JpaRepository<T, ID> {}
