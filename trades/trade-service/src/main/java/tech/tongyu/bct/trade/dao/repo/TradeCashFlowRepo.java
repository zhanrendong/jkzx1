package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.dao.dbo.TradeCashFlow;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TradeCashFlowRepo extends JpaRepository<TradeCashFlow, UUID> {

    void deleteByTradeId(String tradeId);

    List<TradeCashFlow> findAllByTradeId(String tradeId);

    List<TradeCashFlow> findAllByPositionId(String positionId);

    List<TradeCashFlow> findByCreatedAtBetween(Instant before, Instant after);

    List<TradeCashFlow> findByTradeIdAndPositionIdAndLcmEventType(String tradeId, String positionId,
                                                                  LCMEventTypeEnum lcmEventType);
}
