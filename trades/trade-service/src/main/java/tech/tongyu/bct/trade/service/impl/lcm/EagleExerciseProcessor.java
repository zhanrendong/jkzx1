package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedEagleOption;
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
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class EagleExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public EagleExerciseProcessor(LCMEventRepo lcmEventRepo){
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Lists.newArrayList();
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
        if (!(instrument instanceof AnnualizedEagleOption)) {
            throw new CustomException(String.format("AnnualizedEagleOption只支持鹰式期权行权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }

        checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedEagleOption) {
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException("请输入鹰式期权结算金额settleAmount");
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

    public static BigDecimal calEagleUnitPayment(BigDecimal strike1, BigDecimal strike2, BigDecimal strike3, BigDecimal strike4, BigDecimal lowParticipationRate, BigDecimal highParticipationRate, BigDecimal spot){
        return calLeftEagleUnitPayment(strike1, strike2, lowParticipationRate, spot)
                .add(calRightEagleUnitPayment(strike3, strike4, highParticipationRate, spot))
                .add(calMaximumUnitPayment(strike1, strike2, strike3, lowParticipationRate, spot));
    }

    public static BigDecimal calMaximumUnitPayment(BigDecimal strike1, BigDecimal strike2, BigDecimal strike3, BigDecimal lowParticipationRate, BigDecimal spot){
        return spot.compareTo(strike2) >= 0 && spot.compareTo(strike3) <= 0
                ? strike2.subtract(strike1).multiply(lowParticipationRate)
                : BigDecimal.ZERO;
    }

    public static BigDecimal calLeftEagleUnitPayment(BigDecimal strike1, BigDecimal strike2, BigDecimal lowParticipationRate, BigDecimal spot){
        BigDecimal lowDiff = spot.subtract(strike1);
        return (lowDiff.compareTo(BigDecimal.ZERO) > 0
                ? strike2.subtract(spot).compareTo(BigDecimal.ZERO) > 0
                    ? lowDiff
                    : BigDecimal.ZERO
                : BigDecimal.ZERO).multiply(lowParticipationRate);
    }

    public static BigDecimal calRightEagleUnitPayment(BigDecimal strike3, BigDecimal strike4, BigDecimal highParticipationRate, BigDecimal spot){
        BigDecimal highDiff = strike4.subtract(spot);
        return (highDiff.compareTo(BigDecimal.ZERO) > 0
                ? spot.subtract(strike3).compareTo(BigDecimal.ZERO) > 0
                    ? highDiff
                    : BigDecimal.ZERO
                : BigDecimal.ZERO).multiply(highParticipationRate);
    }

    public static BigDecimal calCount(BigDecimal notional, BigDecimal initSpot){
        if(initSpot.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return notional.divide(initSpot, 8, RoundingMode.DOWN);
    }

    public static BigDecimal calEaglePayment(BigDecimal strike1, BigDecimal strike2, BigDecimal strike3, BigDecimal strike4
            , BigDecimal lowParticipationRate, BigDecimal highParticipationRate, BigDecimal spot, BigDecimal notional, BigDecimal initSpot){
        return calEagleUnitPayment(strike1, strike2, strike3, strike4, lowParticipationRate, highParticipationRate, spot)
                .multiply(calCount(notional, initSpot));
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position, eventDto.eventDetail);
        AnnualizedEagleOption annualizedEagleOption = (AnnualizedEagleOption) position.asset.instrumentOfValue();

        return calEaglePayment(
                annualizedEagleOption.strike1Value(),
                annualizedEagleOption.strike2Value(),
                annualizedEagleOption.strike3Value(),
                annualizedEagleOption.strike4Value(),
                annualizedEagleOption.lowParticipationRate(),
                annualizedEagleOption.highParticipationRate(),
                getUnderlyerPrice(eventDto.getEventDetail()),
                annualizedEagleOption.notionalAmountValue(),
                annualizedEagleOption.initialSpot()
        );
    }
}
