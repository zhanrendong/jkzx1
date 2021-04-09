package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.dao.dbo.LCMNotification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface LCMNotificationRepo extends JpaRepository<LCMNotification, UUID> {
    List<LCMNotification> findAllByTradeId(String tradeId);

    List<LCMNotification> findAllByPositionId(String positionId);

    List<LCMNotification> findAllByTradeIdIn(List<String> tradeIds);

    List<LCMNotification> findAllByPositionIdIn(List<String> positionIds);

    List<LCMNotification> findByTradeIdInAndNotificationTimeBetween(List<String> tradeIds,
                                                                    LocalDateTime start, LocalDateTime end);

    List<LCMNotification> findByTradeIdInAndNotificationEventTypeAndNotificationTimeBetween(List<String> tradeIds,
                                                                                            LCMEventTypeEnum notificationEventType,
                                                                                            LocalDateTime start, LocalDateTime end);

    List<LCMNotification> findByNotificationTimeBetween(LocalDateTime start, LocalDateTime end);

    List<LCMNotification> findByNotificationEventTypeAndNotificationTimeBetween(LCMEventTypeEnum notificationEventType,
                                                                                LocalDateTime start, LocalDateTime end);
}
