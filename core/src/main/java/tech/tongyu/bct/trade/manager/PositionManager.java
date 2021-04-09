package tech.tongyu.bct.trade.manager;

import tech.tongyu.bct.cm.trade.impl.BctTradePosition;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PositionManager {

    default void upsert(BctTradePosition bctPosition){
        upsert(bctPosition, null);
    }

    default void deleteByPositionId(String positionId){
        deleteByPositionId(positionId, null, null);
    }

    default Optional<BctTradePosition> getByPositionId(String positionId){
        return getByPositionId(positionId, null, null);
    }

    default Optional<List<BctTradePosition>> getByPositionIds(List<String> positionIds){
        return getByPositionIds(positionIds, null, null);
    }

    void deleteAll();

    void upsert(BctTradePosition bctPosition, OffsetDateTime validTime);

    void deleteByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    Optional<BctTradePosition> getByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    Optional<List<BctTradePosition>> getByPositionIds(List<String> positionIds, OffsetDateTime validTime, OffsetDateTime transactionTime);

    boolean existByPositionId(String positionId);
}
