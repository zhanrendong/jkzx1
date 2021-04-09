package tech.tongyu.bct.trade.service;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LCMProcessor {

    String PAYMENT_DATE = "paymentDate";

    LCMEventTypeEnum eventType();

    List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset);

    List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto);

    default boolean isStatusError(LCMEventTypeEnum nowStatus) {
        return LCMEventTypeEnum.SETTLE.equals(nowStatus) || LCMEventTypeEnum.UNWIND.equals(nowStatus) ||
                LCMEventTypeEnum.EXERCISE.equals(nowStatus) || LCMEventTypeEnum.KNOCK_OUT.equals(nowStatus) ||
                LCMEventTypeEnum.EXPIRATION.equals(nowStatus) || LCMEventTypeEnum.SNOW_BALL_EXERCISE.equals(nowStatus);
    }

    default LocalDate getPaymentDate(Map<String, Object> eventDetail) {
        LocalDate paymentDate = LocalDate.now();
        if (CollectionUtils.isNotEmpty(eventDetail)) {
            String paymentDateStr = (String) eventDetail.get(PAYMENT_DATE);
            if (StringUtils.isNotBlank(paymentDateStr)) {
                paymentDate = LocalDate.parse(paymentDateStr);
            }
        }
        return paymentDate;
    }
}
