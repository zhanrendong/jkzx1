package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.trade.dto.event.CashFlowCollectionDTO;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMUnwindAmountDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TradeLCMService {

    void repairHistoryTradeCashFlow();

    List<LCMUnwindAmountDTO> getTradesUnwindAmount(List<String> tradeIds);

    List<LCMEventDTO> listAllCashFlow();

    Boolean lcmEventExistByUUID(UUID uuid);

    BigDecimal exercisePreSettle(LCMEventDTO eventDto);

    List<CashFlowCollectionDTO> listCashFlowCollection();

    List<LCMEventDTO> listCashFlowsByTradeId(String tradeId);

    List<LCMEventDTO> listLCMEventsByTradeId(String tradeId);

    List<LCMEventDTO> listLCMEventsByTradeIdAndPositionId(String tradeId, String positionId);

    List<LCMEventDTO> listLCMEventsByPositionId(String positionId);

    List<LCMEventDTO> listLCMEventsByPositionIds(List<String> positionIds);

    List<LCMEventDTO> listCashFlowsByDate(LocalDate date);

    List<LCMNotificationDTO> generateLCMEvents(String tradeId, OffsetDateTime validTime);

    List<LCMNotificationDTO> reGenerateLCMEvents(String tradeId, OffsetDateTime validTime);

    List<LCMNotificationDTO> loadLCMEvents(String tradeId);

    List<LCMNotificationDTO> loadLCMEventsSearch(List<String> tradeIds, LCMEventTypeEnum notificationEventType,
                                                 LocalDateTime start, LocalDateTime end);

    List<LCMNotificationDTO> loadLCMEventsBefore(String tradeId, LocalDateTime before);

    List<LCMNotificationDTO> loadLCMEventsAfter(String tradeId, LocalDateTime after);

    List<LCMNotificationDTO> loadLCMEventsBetween(String tradeId, LocalDateTime before, LocalDateTime after);

    LCMUnwindAmountDTO getUnwindAmounts(String tradeId, String positionId);

    BctTrade processLCMEvent(LCMEventDTO event);

    List<LCMEventTypeEnum> getSupportedTradeLCMEventType(String tradeId, OffsetDateTime validTime);
}
