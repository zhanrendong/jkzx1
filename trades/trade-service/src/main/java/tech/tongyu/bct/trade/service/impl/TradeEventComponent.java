package tech.tongyu.bct.trade.service.impl;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.trade.dto.event.TradeEventDTO;
import tech.tongyu.bct.trade.dto.TradeEventTypeEnum;

@Component
public class TradeEventComponent {

    public TradeEventDTO createTradeEvent(TradeEventTypeEnum tradeEventType) {
        TradeEventDTO tradeEvent = new TradeEventDTO();
//        tradeEvent.tradeEventType = tradeEventType;
        // TODO(http://jira.tongyu.tech:8080/browse/OTMS-1394): 暂时的placeholder，其他服务实现以后再完善
        tradeEvent.userLoginId = "";
        return tradeEvent;
    }
}
