package tech.tongyu.core.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.core.postgres.type.PGTsTzRange;
import tech.tongyu.core.postgres.type.TsTzRange;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseBitemporalRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    String table();

    UUID insert(T entity, int status, UUID applicationEventId, OffsetDateTime valid_from, OffsetDateTime valid_to) throws Exception;

    @Transactional
    default UUID insert(T entity, int status, UUID applicationEventId, OffsetDateTime valid) throws Exception {
        return insert(entity, status, applicationEventId, valid, TsTzRange.INFINITY.minusYears(7981));
    }

    @Transactional
    default UUID append(T entity, UUID applicationEventId, OffsetDateTime valid) throws Exception {
        return insert(entity, 1, applicationEventId, valid, TsTzRange.INFINITY);
    }

    @Transactional
    default UUID remove(T entity, UUID applicationEventId, OffsetDateTime valid) throws Exception {
        return insert(entity, 0, applicationEventId, valid, TsTzRange.INFINITY);
    }

    T find(UUID entityId, OffsetDateTime valid, OffsetDateTime system);

    List<T> find(List<UUID> entityId, OffsetDateTime valid, OffsetDateTime system);

    default T find(UUID entityId) {
        OffsetDateTime now = OffsetDateTime.now();
        return find(entityId, now, now);
    }

    List<T> versions(UUID entityId);
}
