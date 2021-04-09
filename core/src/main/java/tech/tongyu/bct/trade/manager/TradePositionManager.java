package tech.tongyu.bct.trade.manager;

import tech.tongyu.bct.cm.trade.impl.BctTrade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TradePositionManager {

    void deleteAll();

    List<BctTrade> findAll();

    List<BctTrade> findByExample(BctTrade bctTrade);

    void upsert(BctTrade bctTrade, OffsetDateTime validTime);

    void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    Optional<BctTrade> getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<BctTrade> findByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime);

    default Optional<BctTrade> getByTradeId(String tradeId){
        return getByTradeId(tradeId, null, null);
    }

}
