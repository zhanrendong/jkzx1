package tech.tongyu.bct.trade.manager;

import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TradeManager {

    default void upsert(BctTrade bctTrade){
        upsert(bctTrade, null);
    }

    default void deleteByTradeId(String tradeId){
        deleteByTradeId(tradeId, null, null);
    }

    default Optional<BctTrade> getByTradeId(String tradeId){
        return getByTradeId(tradeId, null, null);
    }

    default List<BctTrade> findByBookName(String bookName){
        return findByBookName(bookName, null, null);
    }

    default List<BctTrade> findByTrader(String trader){
        return findByTrader(trader, OffsetDateTime.now(), OffsetDateTime.now());
    }

    void deleteAll();

    List<BctTrade> findAll();

    List<BctTrade> findByTradeIds(List<String> tradeIds);

    List<BctTrade> findByExample(BctTrade bctTrade);

    List<BctTrade> findBySimilarTradeId(String tradeId);

    List<BctTrade> findByTradeStatus(TradeStatusEnum tradeStatus);

    void upsert(BctTrade bctTrade, OffsetDateTime validTime);

    void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    Optional<BctTrade> getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<BctTrade> findByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<BctTrade> findByTrader(String trader, OffsetDateTime validTime, OffsetDateTime transactionTime);

    boolean existsByTradeId(String tradeId);
}
