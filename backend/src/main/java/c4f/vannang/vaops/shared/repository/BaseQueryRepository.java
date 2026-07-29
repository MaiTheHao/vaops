package c4f.vannang.vaops.shared.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface BaseQueryRepository<T, ID> extends Repository<T, ID>, JpaSpecificationExecutor<T> {
}
