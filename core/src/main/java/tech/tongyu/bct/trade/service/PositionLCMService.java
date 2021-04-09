package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;

import java.time.OffsetDateTime;
import java.util.List;

public interface PositionLCMService {
    List<LCMNotificationInfoDTO> generatePositionLCMEvents(BctTradePosition position);

    List<LCMEventTypeEnum> getSupportedPositionLCMEventType(String positionId, OffsetDateTime validTime);

    List<LCMEventTypeEnum> getSupportedPositionLCMEventType(BctTradePosition position);
}
