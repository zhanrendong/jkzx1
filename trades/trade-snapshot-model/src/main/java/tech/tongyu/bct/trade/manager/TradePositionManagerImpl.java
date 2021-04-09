package tech.tongyu.bct.trade.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TradePositionManagerImpl implements TradePositionManager {

    TradeManagerImpl tradeManager;

    PositionManagerImpl positionManager;

    @Autowired
    public TradePositionManagerImpl(TradeManagerImpl tradeManager,
                                    PositionManagerImpl positionManager) {
        this.tradeManager = tradeManager;
        this.positionManager = positionManager;
    }

    @Override
    @Transactional
    public void deleteAll() {
        tradeManager.deleteAll();
        positionManager.deleteAll();
    }

    @Override
    public List<BctTrade> findAll() {
        return tradeManager.findAll()
                .parallelStream()
                .map(this::setPositions)
                .collect(Collectors.toList());
    }

    @Override
    public List<BctTrade> findByExample(BctTrade bctTrade) {
        return tradeManager.findByExample(bctTrade)
                .parallelStream()
                .map(this::setPositions)
                .collect(Collectors.toList());
    }

    @Override
    public void upsert(BctTrade bctTrade, OffsetDateTime validTime) {
        setPositionIds(bctTrade);
        tradeManager.upsert(bctTrade, validTime);
        bctTrade.getPositions()
                .forEach(position -> positionManager.upsert(position, validTime));
    }

    @Override
    @Transactional
    public void deleteByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        BctTrade bctTrade = tradeManager.getByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("交易编号:%s,数据不存在", tradeId)));
        bctTrade.getPositionIds().forEach(positionManager::deleteByPositionId);
        tradeManager.deleteByTradeId(tradeId);
    }

    @Override
    public Optional<BctTrade> getByTradeId(String tradeId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return tradeManager.getByTradeId(tradeId, validTime, transactionTime)
                .map(trade -> {
                    BctTrade bctTrade = setPositions(trade);
                    return Optional.of(bctTrade);
                }).orElse(Optional.empty());
    }

    @Override
    public List<BctTrade> findByBookName(String bookName, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        return tradeManager.findByBookName(bookName, validTime, transactionTime)
                .parallelStream()
                .map(this::setPositions)
                .collect(Collectors.toList());
    }

    private BctTrade setPositions(BctTrade bctTrade){
        List<BctTradePosition> positions = bctTrade.getPositionIds()
                .stream()
                .map(pid -> positionManager.getByPositionId(pid).get())
                .collect(Collectors.toList());
        bctTrade.setPositions(positions);
        return bctTrade;
    }

    private void setPositionIds(BctTrade bctTrade){
        ArrayList<String> positionIds = new ArrayList<>(bctTrade.getPositions()
                .stream()
                .map(BctTradePosition::getPositionId)
                .collect(Collectors.toSet()));
        bctTrade.setPositionIds(positionIds);
    }
}
