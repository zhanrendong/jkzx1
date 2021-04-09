package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.PremiumFeature;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ExerciseCommonProcessor  extends ExerciseProcessorCommon implements LCMProcessor {

    LCMEventRepo lcmEventRepo;

    @Autowired
    public ExerciseCommonProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.EXERCISE;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        return BigDecimal.ZERO;
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        if (isStatusError(position.getLcmEventType())){
            throw new CustomException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行行权操作",
                    position.getPositionId(), position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (instrument instanceof OptionExerciseFeature){
            ProductTypeEnum productType = instrument.productType();
            if (ExerciseTypeEnum.EUROPEAN.equals(((OptionExerciseFeature) instrument).exerciseType())
                    && !ProductTypeEnum.DOUBLE_NO_TOUCH.equals(productType)
                    && !ProductTypeEnum.DOUBLE_TOUCH.equals(productType)){
                LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
                if (expirationDate.isAfter(LocalDate.now())){
                    throw new CustomException(String.format("持仓编号[%s],合约还未到期,不能执行行权操作,到期日[%s]",
                            position.getPositionId(), expirationDate.toString()));
                }
            }
        }

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入行权结算金额settleAmount");
        }
        BigDecimal settleAmount = new BigDecimal(settleAmountStr);
        BigDecimal premium = ((PremiumFeature) instrument).actualPremium();
        premium = getPremiumValueByPartyRole(premium, position.partyRole());

        LCMEvent lcmEvent = new LCMEvent();
        BeanUtils.copyProperties(eventDto, lcmEvent);
        lcmEvent.setEventDetail(JsonUtils.mapper.valueToTree(eventDetail));
        lcmEvent.setPaymentDate(getPaymentDate(eventDetail));
        lcmEvent.setEventType(eventDto.getLcmEventType());
        lcmEvent.setCashFlow(settleAmount);
        lcmEvent.setPremium(premium);
        lcmEventRepo.save(lcmEvent);

        sendPositionDoc(trade, position);
        position.setLcmEventType(eventDto.getLcmEventType());
        BctTradePosition newPosition = new BctTradePosition();
        BeanUtils.copyProperties(position, newPosition);
        return Arrays.asList(newPosition);
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }
}
