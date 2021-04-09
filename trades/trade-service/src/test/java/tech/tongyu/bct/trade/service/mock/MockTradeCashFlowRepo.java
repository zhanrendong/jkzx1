package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.trade.dao.dbo.TradeCashFlow;
import tech.tongyu.bct.trade.dao.repo.TradeCashFlowRepo;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MockTradeCashFlowRepo extends MockJpaRepository<TradeCashFlow> implements TradeCashFlowRepo {
    public MockTradeCashFlowRepo() {
        super(new LinkedList<>());
    }

    @Override
    public List<TradeCashFlow> findByTradeIdAndPositionIdAndLcmEventType(String tradeId, String positionId, LCMEventTypeEnum lcmEventType) {
        return null;
    }

    @Override
    public List<TradeCashFlow> findAllByTradeId(String tradeId) {
        return data.stream().filter(t->t.getTradeId().equals(tradeId)).collect(Collectors.toList());
    }

    @Override
    public List<TradeCashFlow> findAllByPositionId(String positionId) {
        return null;
    }

    @Override
    public List<TradeCashFlow> findByCreatedAtBetween(Instant before, Instant after) {
        return null;
    }

    @Override
    public void deleteByTradeId(String tradeId) {

    }
}
