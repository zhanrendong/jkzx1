package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class PaymentOptionProcessor implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private static final String CASH_FLOW = "cashFlow";

    @Autowired
    public PaymentOptionProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.PAYMENT;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String cashFlowStr = (String) eventDetail.get(CASH_FLOW);
        if (StringUtils.isBlank(cashFlowStr)) {
            throw new CustomException("请输入现金流金额cashFlow");
        }
        BigDecimal cashFlow = new BigDecimal(cashFlowStr);

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.defaultNumberValue();
        lcmEvent.setCashFlow(cashFlow);
        lcmEventRepo.save(lcmEvent);

        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }
}
