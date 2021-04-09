package tech.tongyu.core.postgres;

import java.util.UUID;

public interface BaseBitemporalService<
        E extends BaseBitemporalEntity,
        EKey extends BaseRelationalEntity,
        KeyRepo extends RelationalRepository<EKey, UUID>,
        BitempRepo extends BitemporalRepository<E, UUID>> {

    KeyRepo keyRepo();

    BitempRepo bitempRepo();

    default void persist(BaseBitemporalEntity entity) {

    }
}
