package tech.tongyu.core.postgres;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import java.util.UUID;

@NoRepositoryBean
public abstract class RelationalRepository<T extends BaseRelationalEntity, ID extends UUID>
        extends SimpleJpaRepository<T, ID> implements BaseRelationalRepository<T, ID> {

    private final EntityManager entityManager;

    public RelationalRepository(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
    }
}
