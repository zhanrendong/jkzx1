package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.trade.dao.dbo.LCMNotification;
import tech.tongyu.bct.trade.dao.repo.LCMNotificationRepo;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MockLCMNotificationRepo extends MockJpaRepository<LCMNotification> implements LCMNotificationRepo {

    public MockLCMNotificationRepo() {
        super(new LinkedList<>());
    }

    @Override
    public List<LCMNotification> findAllByTradeId(String tradeId) {
        return data.stream().filter(n -> n.getTradeId().equals(tradeId)).collect(Collectors.toList());
    }

    @Override
    public List<LCMNotification> findAllByPositionId(String positionId) {
        return data.stream().filter(n -> n.getPositionId().equals(positionId)).collect(Collectors.toList());
    }

    @Override
    public List<LCMNotification> findAllByTradeIdIn(List<String> tradeIds) {
        return data.stream().filter(n -> tradeIds.contains(n.getTradeId())).collect(Collectors.toList());
    }

    @Override
    public List<LCMNotification> findAllByPositionIdIn(List<String> positionIds) {
        return data.stream().filter(n -> positionIds.contains(n.getPositionId())).collect(Collectors.toList());
    }

    @Override
    public List<LCMNotification> findByNotificationTimeBetween(LocalDateTime start, LocalDateTime end) {
        return null;
    }

    @Override
    public List<LCMNotification> findByNotificationEventTypeAndNotificationTimeBetween(LCMEventTypeEnum notificationEventType, LocalDateTime start, LocalDateTime end) {
        return null;
    }

    @Override
    public List<LCMNotification> findByTradeIdInAndNotificationTimeBetween(List<String> tradeIds, LocalDateTime start, LocalDateTime end) {
        return null;
    }

    @Override
    public List<LCMNotification> findByTradeIdInAndNotificationEventTypeAndNotificationTimeBetween(List<String> tradeIds, LCMEventTypeEnum notificationEventType, LocalDateTime start, LocalDateTime end) {
        return null;
    }
}
