package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.trade.manager.TradeManager;

import java.time.OffsetDateTime;
import java.util.*;

public class MockTradeManager implements TradeManager {
    private Map<String, BctTrade> repo = new LinkedHashMap();

    @Override
    public void deleteAll() {
    }

    @Override
    public List<BctTrade> findByTradeIds(List<String> tradeIds) {
        return null;
    }

    @Override
    public void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {

    }

    @Override
    public List<BctTrade> findByExample(BctTrade bctTrade) {
        return null;
    }

    @Override
    public List<BctTrade> findBySimilarTradeId(String tradeId) {
        return null;
    }

    @Override
    public List<BctTrade> findAll() {
        return null;
    }

    @Override
    public void upsert(BctTrade bctTrade, OffsetDateTime validTime) {
        repo.put(bctTrade.getTradeId(), bctTrade);
    }

    @Override
    public Optional<BctTrade> getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        BctTrade bctTrade = repo.get(tradeId);
        if (Objects.isNull(bctTrade)){
            return Optional.empty();
        }

        return Optional.of(bctTrade);
    }

    @Override
    public List<BctTrade> findByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime) {

        return null;
    }

    @Override
    public List<BctTrade> findByTrader(String trader, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return null;
    }

    @Override
    public boolean existsByTradeId(String tradeId) {
        return false;
    }

    @Override
    public List<BctTrade> findByTradeStatus(TradeStatusEnum tradeStatus) {
        return null;
    }
}