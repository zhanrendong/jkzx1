package tech.tongyu.bct.reference.service.mock;

import com.google.common.collect.Lists;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.trade.dto.event.CashFlowCollectionDTO;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMUnwindAmountDTO;
import tech.tongyu.bct.trade.service.TradeLCMService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class MockTradeLCMServiceImpl implements TradeLCMService {

    @Override
    public void repairHistoryTradeCashFlow() {
    }

    @Override
    public List<LCMUnwindAmountDTO> getTradesUnwindAmount(List<String> tradeIds) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listAllCashFlow() {
        return Lists.newArrayList();
    }

    @Override
    public Boolean lcmEventExistByUUID(UUID uuid) {
        return true;
    }

    @Override
    public BigDecimal exercisePreSettle(LCMEventDTO eventDto) {
        return BigDecimal.ZERO;
    }

    @Override
    public List<CashFlowCollectionDTO> listCashFlowCollection() {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listCashFlowsByTradeId(String tradeId) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByTradeId(String tradeId) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByTradeIdAndPositionId(String tradeId, String positionId) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByPositionId(String positionId) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listLCMEventsByPositionIds(List<String> positionIds) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMEventDTO> listCashFlowsByDate(LocalDate date) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> generateLCMEvents(String tradeId, OffsetDateTime validTime) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> reGenerateLCMEvents(String tradeId, OffsetDateTime validTime) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEvents(String tradeId) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsSearch(List<String> tradeIds, LCMEventTypeEnum notificationEventType, LocalDateTime start, LocalDateTime end) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsBefore(String tradeId, LocalDateTime before) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsAfter(String tradeId, LocalDateTime after) {
        return Lists.newArrayList();
    }

    @Override
    public List<LCMNotificationDTO> loadLCMEventsBetween(String tradeId, LocalDateTime before, LocalDateTime after) {
        return Lists.newArrayList();
    }

    @Override
    public LCMUnwindAmountDTO getUnwindAmounts(String tradeId, String positionId) {
        return null;
    }

    @Override
    public BctTrade processLCMEvent(LCMEventDTO event) {
        return new BctTrade();
    }

    @Override
    public List<LCMEventTypeEnum> getSupportedTradeLCMEventType(String tradeId, OffsetDateTime validTime) {
        return Lists.newArrayList();
    }
}
