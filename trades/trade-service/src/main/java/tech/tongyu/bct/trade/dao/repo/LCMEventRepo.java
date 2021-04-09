package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface LCMEventRepo extends JpaRepository<LCMEvent, UUID> {

    void deleteByTradeId(String tradeId);

    List<LCMEvent> findAllByTradeId(String tradeId);

    List<LCMEvent> findAllByPositionId(String positionId);

    List<LCMEvent> findAllByPositionIdIn(List<String> positionIds);

    List<LCMEvent> findByCreatedAtBetween(Instant start, Instant end);

    List<LCMEvent> findAllByTradeIdAndPositionId(String tradeId, String positionId);

    List<LCMEvent> findByPositionIdAndEventTypeInOrderByCreatedAt(String position, List<LCMEventTypeEnum> eventTypes);

    List<LCMEvent> findByPositionIdInAndEventTypeIn(Set<String> positionIds, List<LCMEventTypeEnum> eventTypes);

    List<LCMEvent> findByTradeIdAndPaymentDateAndEventTypeIn(String tradeId, LocalDate paymentDate, List<LCMEventTypeEnum> eventTypes);

    List<LCMEvent> findByPaymentDateAndEventTypeIn(LocalDate paymentDate, List<LCMEventTypeEnum> eventTypes);
}
