package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.trade.dao.dbo.TradeEvent;
import tech.tongyu.bct.trade.dao.repo.TradeEventRepo;

import java.util.LinkedList;

public class MockTradeEventRepo extends MockJpaRepository<TradeEvent> implements TradeEventRepo {

    public MockTradeEventRepo(){
        super(new LinkedList<>());
    }

}
