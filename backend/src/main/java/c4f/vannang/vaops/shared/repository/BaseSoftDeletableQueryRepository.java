package c4f.vannang.vaops.shared.repository;

import c4f.vannang.vaops.shared.base.BaseSoftDeletableEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

/**
 * Base repository for soft-deletable entities.
 *
 * @param <T>  the soft-deletable entity type
 * @param <ID> the entity id type
 */
@NoRepositoryBean
public interface BaseSoftDeletableQueryRepository<T extends BaseSoftDeletableEntity, ID>
    extends BaseQueryRepository<T, ID> {

  @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
  Optional<T> findById(@Param("id") ID id);

  @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
  Optional<T> findByIdWithDeleted(@Param("id") ID id);

  @Query(
      "SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.active = true AND e.deletedAt IS NULL")
  Optional<T> findActiveById(@Param("id") ID id);

  @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.deletedAt IS NULL")
  List<T> findAllByIdIn(@Param("ids") List<ID> ids);

  @Query("SELECT e FROM #{#entityName} e WHERE e.id IN :ids AND e.active = true AND e.deletedAt IS"
      + " NULL")
  List<T> findAllActiveByIdIn(@Param("ids") List<ID> ids);

  @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id ="
      + " :id")
  boolean existsByIdWithDeleted(@Param("id") ID id);
}
