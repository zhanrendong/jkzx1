package tech.tongyu.bct.trade.dao.dto.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.trade.dao.dto.TradeEvent;

import java.util.List;
import java.util.UUID;

public interface TradeEventRepo extends JpaRepository<TradeEvent, UUID> {

    List<TradeEvent> findAllByTradeVersionUUID(UUID tradeVersionUUID);

    List<TradeEvent> findAllByUserLoginId(String userLoginId);
}
