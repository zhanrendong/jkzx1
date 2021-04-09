package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVerticalSpreadOption;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VerticalSpreadExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public VerticalSpreadExerciseProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.EXERCISE;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        InstrumentOfValue instrument = position.asset.instrumentOfValue();

        BigDecimal settleAmount = calVerticalSpreadExercisePrice(instrument, eventDetail);
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Collections.emptyList();
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入结算金额settleAmount");
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

    private void checkPositionStatus(BctTradePosition position){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能行权",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedVerticalSpreadOption)) {
            throw new IllegalArgumentException(
                    String.format("VerticalSpreadExerciseProcessor只支持VerticalSpreadOption,与Position[%s]的结构类型[%s]不匹配",
                            position.positionId, instrument.getClass()));
        }
        AnnualizedVerticalSpreadOption verticalSpreadOption = (AnnualizedVerticalSpreadOption) instrument;
        if (ExerciseTypeEnum.EUROPEAN.equals(verticalSpreadOption.exerciseType)){
            checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
        }
    }

    private BigDecimal calVerticalSpreadExercisePrice(InstrumentOfValue instrument, Map<String, Object> eventDetail){
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)){
            throw new IllegalArgumentException("请输入标的物价格");
        }
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);
        BigDecimal initialSpot = BigDecimal.ZERO;
        BigDecimal notional = BigDecimal.ZERO;
        BigDecimal spread = BigDecimal.ZERO;
        if (instrument instanceof AnnualizedVerticalSpreadOption) {
            AnnualizedVerticalSpreadOption verticalSpreadOption = (AnnualizedVerticalSpreadOption) instrument;
            notional = verticalSpreadOption.notionalWithParticipation();
            initialSpot = verticalSpreadOption.initialSpot();
            List strikeList = verticalSpreadOption.strikes();
            List<BigDecimal> strikes = (List<BigDecimal>) strikeList
                    .stream()
                    .map(strike -> ((UnitOfValue<BigDecimal>) strike).value())
                    .collect(Collectors.toList());
            if (strikes.size() != 2) {
                throw new RuntimeException("number of strike is not 2");
            }
            Collections.sort(strikes);
            BigDecimal lowStrike = strikes.get(0);
            BigDecimal highStrike = strikes.get(1);
            OptionTypeEnum optionType = verticalSpreadOption.optionType();
            switch (optionType){
                case PUT: // 看跌
                    // if (S < strikeLow) then strikeHigh - strikeLow;
                    if (underlyerPrice.compareTo(lowStrike) < 0){
                        spread = highStrike.subtract(lowStrike);
                    }
                    // if (strikeLow<=S<strikeHigh) then strikeHigh - S;
                    if (underlyerPrice.compareTo(BigDecimal.ZERO) >= 0 &&
                            underlyerPrice.compareTo(highStrike) < 0){
                        spread = highStrike.subtract(underlyerPrice);
                    }
                    break;
                case CALL: // 看涨
                    // if (S>=strikeHigh) then strikeHigh-strikeLow)
                    if (underlyerPrice.compareTo(highStrike) >= 0){
                        spread = highStrike.subtract(lowStrike);
                    }
                    //  if (strikeLow<=S<strikeHigh) then S-strikeLow;
                    if (underlyerPrice.compareTo(lowStrike) >= 0 &&
                            underlyerPrice.compareTo(highStrike) < 0){
                        spread = underlyerPrice.subtract(lowStrike);
                    }
                    break;
            }
        }
        if (initialSpot.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        // notional / initialSpot * spread
        return notional.divide(initialSpot, 8, BigDecimal.ROUND_DOWN).multiply(spread);
    }
}
