package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDoubleSharkFinOption;
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
public class DoubleSharkExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public DoubleSharkExerciseProcessor(LCMEventRepo lcmEventRepo){
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
        if (!(instrument instanceof AnnualizedDoubleSharkFinOption)) {
            throw new CustomException(String.format("AnnualizedDoubleSharkFinOption只支持双鲨期权行权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }

        AnnualizedDoubleSharkFinOption instr = (AnnualizedDoubleSharkFinOption) instrument;
        if(hasKnockedOut(instr.lowBarrierValue(), instr.highBarrierValue(), getUnderlyerPrice(eventDTO))){
            throw new CustomException("按照给定标的价格，该双鲨期权已经敲出");
        }

        checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
    }

    public static boolean hasKnockedOut(BigDecimal lowBarrier, BigDecimal highBarrier, BigDecimal spot){
        return lowBarrier.compareTo(spot) > 0
                || highBarrier.compareTo(spot) < 0;
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedDoubleSharkFinOption) {
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException("请输入双鲨结算金额settleAmount");
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

    public static BigDecimal calDoubleSharkUnitPayment(BigDecimal lowStrike, BigDecimal highStrike, BigDecimal spot, BigDecimal lowParticipationRate, BigDecimal highParticipationRate){
        return calLeftFinUnitPayment(lowStrike, lowParticipationRate, spot).add(calRighFinUnitPayment(highStrike, highParticipationRate, spot));
    }

    public static BigDecimal calCount(BigDecimal notional, BigDecimal initSpot){
        if(initSpot.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        return notional.divide(initSpot, 8, RoundingMode.DOWN);
    }

    public static BigDecimal calLeftFinUnitPayment(BigDecimal lowStrike, BigDecimal lowParticipationRate, BigDecimal spot){
        BigDecimal lowUnitProfit = lowStrike.subtract(spot);
        lowUnitProfit = lowUnitProfit.compareTo(BigDecimal.ZERO) > 0 ? lowUnitProfit : BigDecimal.ZERO;
        return lowUnitProfit.multiply(lowParticipationRate);
    }

    public static BigDecimal calRighFinUnitPayment(BigDecimal highStrike, BigDecimal highParticipationRate, BigDecimal spot){
        BigDecimal highUnitProfit = spot.subtract(highStrike);
        highUnitProfit = highUnitProfit.compareTo(BigDecimal.ZERO) > 0 ? highUnitProfit : BigDecimal.ZERO;
        return highUnitProfit.multiply(highParticipationRate);
    }

    public static BigDecimal calDoubleSharkPayment(BigDecimal lowStrike, BigDecimal highStrike,
                                                   BigDecimal lowParticipationRate, BigDecimal highParticipationRate,
                                                   BigDecimal spot, BigDecimal notional, BigDecimal initSpot){
        return calDoubleSharkUnitPayment(lowStrike, highStrike, spot, lowParticipationRate, highParticipationRate).multiply(calCount(notional, initSpot));
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position, eventDto.eventDetail);
        AnnualizedDoubleSharkFinOption annualizedDoubleSharkFinOption = (AnnualizedDoubleSharkFinOption) position.asset.instrumentOfValue();
        return calDoubleSharkPayment(annualizedDoubleSharkFinOption.lowStrikeValue()
                , annualizedDoubleSharkFinOption.highStrikeValue()
                , getUnderlyerPrice(eventDto.getEventDetail())
                , annualizedDoubleSharkFinOption.lowParticipationRate()
                , annualizedDoubleSharkFinOption.highParticipationRate()
                , annualizedDoubleSharkFinOption.notionalAmountValue(), annualizedDoubleSharkFinOption.initialSpot());
    }
}
