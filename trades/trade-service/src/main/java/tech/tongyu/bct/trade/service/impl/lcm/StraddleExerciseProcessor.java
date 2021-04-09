package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedStraddleOption;
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
public class StraddleExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public StraddleExerciseProcessor(LCMEventRepo lcmEventRepo){
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Lists.newArrayList();
    }

    private void checkPositionStatus(BctTradePosition position, Map<String, Object> eventDTO){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行权操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedStraddleOption)) {
            throw new CustomException(String.format("AnnualizedDoubleTouchOption只支持跨式期权行权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }

        AnnualizedStraddleOption instr = (AnnualizedStraddleOption) instrument;
        if(instr.lowStrikeValue().compareTo(instr.highStrikeValue()) >= 0){
            throw new CustomException("该跨式期权低行权价大于等于高行权价，无法行权结算");
        }

        checkEuropeanOptionExpirationDate(position.positionId, position.asset.instrumentOfValue());
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedStraddleOption) {
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException("请输入跨式期权结算金额settleAmount");
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
        checkPositionStatus(position, eventDto.eventDetail);
        AnnualizedStraddleOption annualizedDoubleSharkFinOption = (AnnualizedStraddleOption) position.asset.instrumentOfValue();
        return calStraddlePayment(annualizedDoubleSharkFinOption.lowStrikeValue()
                , annualizedDoubleSharkFinOption.highStrikeValue()
                , getUnderlyerPrice(eventDto.getEventDetail())
                , annualizedDoubleSharkFinOption.notionalAmountValue()
                , annualizedDoubleSharkFinOption.initialSpot()
                , annualizedDoubleSharkFinOption.lowParticipationRate()
                , annualizedDoubleSharkFinOption.highParticipationRate()
        );
    }

    public static BigDecimal calStraddleUnitPayment(BigDecimal lowStrike, BigDecimal highStrike, BigDecimal spot, BigDecimal lowParticipationRate, BigDecimal highParticipationRate){
        return calLeftStraddleUnitPayment(lowStrike, spot, lowParticipationRate)
                .add(calRightStraddleUnitPayment(highStrike, spot, highParticipationRate));
    }

    public static BigDecimal calLeftStraddleUnitPayment(BigDecimal lowStrike, BigDecimal spot, BigDecimal lowParticipationRate){
        BigDecimal lowDiff = lowStrike.subtract(spot);
        lowDiff = lowDiff.compareTo(BigDecimal.ZERO) > 0 ? lowDiff : BigDecimal.ZERO;
        return lowDiff.multiply(lowParticipationRate);
    }

    public static BigDecimal calRightStraddleUnitPayment(BigDecimal highStrike, BigDecimal spot, BigDecimal highParticipationRate){
        BigDecimal highDiff = spot.subtract(highStrike);
        highDiff = highDiff.compareTo(BigDecimal.ZERO) > 0 ? highDiff : BigDecimal.ZERO;
        return highDiff.multiply(highParticipationRate);
    }

    public static BigDecimal calCount(BigDecimal notional, BigDecimal initSpot){
        if(initSpot.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return notional.divide(initSpot, 8, RoundingMode.DOWN);
    }

    public static BigDecimal calStraddlePayment(BigDecimal lowStrike, BigDecimal highStrike, BigDecimal spot,
                                                BigDecimal notional, BigDecimal initSpot,
                                                BigDecimal lowParticipationRate, BigDecimal highParticipationRate){
        return calStraddleUnitPayment(lowStrike, highStrike, spot, lowParticipationRate, highParticipationRate).multiply(calCount(notional, initSpot));
    }
}
