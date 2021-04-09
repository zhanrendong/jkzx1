package tech.tongyu.bct.trade.service.mock;


import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.trade.dao.dbo.TradePositionIndex;
import tech.tongyu.bct.trade.dao.repo.TradePositionIndexRepo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MockIndexRepo extends MockJpaRepository<TradePositionIndex> implements TradePositionIndexRepo {

    public MockIndexRepo() {
        super(new LinkedList<>());
    }

    @Override
    public void deleteByTradeId(String tradeId) {

    }

    @Override
    public Boolean existsByPositionId(String positionId) {
        return true;
    }

    @Override
    public Optional<TradePositionIndex> findByPositionId(String positionId) {
        return Optional.empty();
    }

    @Override
    public List<TradePositionIndex> findByCounterPartyName(String counterPartyName) {
        return null;
    }

    @Override
    public List<TradePositionIndex> findByProductTypeInAndLcmEventTypeNotIn(List<ProductTypeEnum> productTypes, List<LCMEventTypeEnum> lcmEventTypes) {
        return null;
    }
}
