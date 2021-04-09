package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDoubleDigitalOption;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DoubleDigitalExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public DoubleDigitalExerciseProcessor(LCMEventRepo lcmEventRepo){
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    private void checkPositionStatus(BctTradePosition position, Map<String, Object> eventDTO){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行权操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedDoubleDigitalOption)) {
            throw new CustomException(String.format("AnnualizedDoubleDigitalOption只支持三层阶梯期权行权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }

        checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Lists.newArrayList();
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedDoubleDigitalOption) {
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException("请输入三层阶梯结算金额settleAmount");
            }
            BigDecimal settleAmount = new BigDecimal(settleAmountStr);
            BigDecimal premium = getInitialPremium(position);

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
        else {
            throw new CustomException("期权类型转换错误");
        }
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.EXERCISE;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position, eventDto.getEventDetail());
        AnnualizedDoubleDigitalOption annualizedDoubleDigitalOption = (AnnualizedDoubleDigitalOption) position.asset.instrumentOfValue();
        BigDecimal spot = getUnderlyerPrice(eventDto.getEventDetail());
        BigDecimal payment = BigDecimal.ZERO;
        if(annualizedDoubleDigitalOption.optionType().equals(OptionTypeEnum.CALL)){
            if(spot.compareTo(annualizedDoubleDigitalOption.lowStrikeValue()) >=0){
                payment = annualizedDoubleDigitalOption.lowPaymentValue();
                if(spot.compareTo(annualizedDoubleDigitalOption.highStrikeValue()) >= 0){
                    payment = annualizedDoubleDigitalOption.highPaymentValue();
                }
            }
        }else{
            payment = annualizedDoubleDigitalOption.highPaymentValue();
            if(annualizedDoubleDigitalOption.lowStrikeValue().compareTo(spot) <= 0){
                payment = annualizedDoubleDigitalOption.lowPaymentValue();
                if(annualizedDoubleDigitalOption.highStrikeValue().compareTo(spot) <= 0){
                    payment = BigDecimal.ZERO;
                }
            }
        }
        return payment;
    }
}
