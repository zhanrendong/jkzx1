package tech.tongyu.bct.trade.service.impl.lcm;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.CashFlowDirectionEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.PaymentStateEnum;
import tech.tongyu.bct.cm.product.iov.impl.CashFlow;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CashFlowExerciseProcessor implements LCMProcessor {

    LCMEventRepo lcmEventRepo;

    @Autowired
    public CashFlowExerciseProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.SETTLE;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof CashFlow)) {
            throw new IllegalArgumentException(
                    String.format("CashFlowExerciseProcessor只支持CashFlow,与Position[%s]的结构类型[%s]不匹配",
                            position.positionId, position.asset().instrumentOfValue().getClass()));

        }

        CashFlow cashFlow = (CashFlow) instrument;
        if (cashFlow.paymentState == PaymentStateEnum.PAID) {
            throw new IllegalArgumentException(String.format("持仓编号:(%s),当前持仓已经支付，不能再次支付", position.positionId));
        }

        BigDecimal settleAmount = cashFlow.paymentAmount;
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        eventDetail.put("paymentState", PaymentStateEnum.PAID);

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(null));
        lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.setCashFlow(cashFlow.paymentDirection == CashFlowDirectionEnum.PAY ? settleAmount.negate() : settleAmount);
        lcmEvent.setPremium(BigDecimal.ZERO);
        lcmEventRepo.save(lcmEvent);

        cashFlow.paymentState = PaymentStateEnum.PAID;
        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }
}
