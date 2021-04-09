package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDigitalOption;
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

@Component
public class DigitalExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    LCMEventRepo lcmEventRepo;

    @Autowired
    public DigitalExerciseProcessor(LCMEventRepo lcmEventRepo) {
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
        BigDecimal settleAmount = digitalExercisePrice(instrument, eventDetail);
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入二元行权结算金额settleAmount");
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
        if (isStatusError(position.getLcmEventType())){
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能行权",
                    position.getPositionId(), position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedDigitalOption)) {
            throw new IllegalArgumentException(
                    String.format("DigitalExerciseProcessor只支持DigitalOption,与Position[%s]的结构类型[%s]不匹配",
                            position.positionId, position.asset().instrumentOfValue().getClass()));
        }
        AnnualizedDigitalOption digitalOption = (AnnualizedDigitalOption) instrument;
        if (ExerciseTypeEnum.EUROPEAN.equals(digitalOption.exerciseType)) {
            checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
        }
    }

    private BigDecimal digitalExercisePrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)) {
            throw new IllegalArgumentException("请输入标的物价格");
        }
        Boolean isZero = true;
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);
        if (instrument instanceof AnnualizedDigitalOption) {
            AnnualizedDigitalOption instr = (AnnualizedDigitalOption) instrument;
            BigDecimal triggerPer  = instr.strikeValue();
            int compareResult = (triggerPer).compareTo(underlyerPrice);
            switch (instr.optionType) {
                case PUT: // 看跌：triggerPer*initialSpot >= S return paymentPer*notionalLot*underlyerMultiplier*initialSpot
                    if (compareResult >= 0) {
                        isZero = false;
                    }
                    break;
                case CALL:// 看涨：triggerPer*initialSpot <= S return paymentPer*notionalLot*underlyerMultiplier*initialSpot
                    if (compareResult <= 0) {
                        isZero = false;
                    }
                    break;
            }
            BigDecimal payment = instr.rebateValue();
            if (isZero) {
                return BigDecimal.ZERO;
            }
            return payment;
        } else {
            throw new IllegalArgumentException("期权类型转换错误");
        }

    }
}
