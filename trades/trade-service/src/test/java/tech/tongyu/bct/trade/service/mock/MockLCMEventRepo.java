package tech.tongyu.bct.trade.service.mock;

import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MockLCMEventRepo extends MockJpaRepository<LCMEvent> implements LCMEventRepo {

    public MockLCMEventRepo(){
        super(new LinkedList<>());
    }

    @Override
    public List<LCMEvent> findAllByTradeId(String tradeId) {
        return data.stream().filter(lcm -> tradeId.equals(lcm.getTradeId())).collect(Collectors.toList());
    }

    @Override
    public List<LCMEvent> findAllByPositionId(String positionId) {
        return data.stream().filter(lcm -> positionId.equals(lcm.getPositionId())).collect(Collectors.toList());
    }

    @Override
    public List<LCMEvent> findAllByPositionIdIn(List<String> positionIds) {
        return data.stream().filter(lcm -> positionIds.contains(lcm.getPositionId())).collect(Collectors.toList());
    }

    @Override
    public void deleteByTradeId(String tradeId) {

    }

    @Override
    public List<LCMEvent> findByPositionIdAndEventTypeInOrderByCreatedAt(String position, List<LCMEventTypeEnum> eventTypes) {
        return null;
    }

    @Override
    public List<LCMEvent> findByPositionIdInAndEventTypeIn(Set<String> positionIds, List<LCMEventTypeEnum> eventTypes) {
        return null;
    }

    @Override
    public List<LCMEvent> findAllByTradeIdAndPositionId(String tradeId, String positionId) {
        return null;
    }

    @Override
    public List<LCMEvent> findByCreatedAtBetween(Instant start, Instant end) {
        return null;
    }

    @Override
    public List<LCMEvent> findByTradeIdAndPaymentDateAndEventTypeIn(String tradeId, LocalDate paymentDate, List<LCMEventTypeEnum> eventTypes) {
        return null;
    }

    @Override
    public List<LCMEvent> findByPaymentDateAndEventTypeIn(LocalDate paymentDate, List<LCMEventTypeEnum> eventTypes) {
        return null;
    }
}
