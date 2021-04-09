package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.trade.manager.PositionManager;

import java.time.OffsetDateTime;
import java.util.*;

public class MockTradePositionManager implements PositionManager {

    private Map<String, BctTradePosition> repo = new LinkedHashMap();


    @Override
    public void deleteByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void upsert(BctTradePosition bctPosition, OffsetDateTime validTime) {
        repo.put(bctPosition.getPositionId(), bctPosition);

    }

    @Override
    public Optional<BctTradePosition> getByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        BctTradePosition bctTradePosition = repo.get(positionId);
        if (Objects.isNull(bctTradePosition)){
            return Optional.empty();
        }
        return Optional.of(bctTradePosition);
    }

    @Override
    public Optional<List<BctTradePosition>> getByPositionIds(List<String> positionIds, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return Optional.empty();
    }

    @Override
    public boolean existByPositionId(String positionId) {
        return false;
    }
}
