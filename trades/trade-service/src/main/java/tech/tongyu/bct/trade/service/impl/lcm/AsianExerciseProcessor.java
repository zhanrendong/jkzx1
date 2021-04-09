package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedAsianOption;
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
public class AsianExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public AsianExerciseProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.EXERCISE;
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        BigDecimal settleAmount = calAsianExercisePrice(instrument, eventDetail);
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入亚式行权结算金额settleAmount");
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
            throw new CustomException(String.format("持仓编号[%s],当前持仓状态[%s],不能行权",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedAsianOption)) {
            throw new CustomException(
                    String.format("AsianExerciseProcessor只支持AsianOption,与Position[%s]的结构类型[%s]不匹配",
                            position.positionId, position.asset().instrumentOfValue().getClass()));
        }
        if (instrument instanceof OptionExerciseFeature) {
            LocalDate nowDate = LocalDate.now();
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                throw new CustomException(
                        String.format("持仓编号[%s],当前持仓为亚式,还未到到期,到期日[%s],不能进行行权操作",
                                position.positionId, expirationDate.toString()));
            }
        }
    }

    private BigDecimal calAsianExercisePrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)) {
            throw new IllegalArgumentException("请输入标的物价格underlyerPrice");
        }
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);
        BigDecimal initialSpot = BigDecimal.ZERO;
        BigDecimal notional = BigDecimal.ZERO;
        BigDecimal spread = BigDecimal.ZERO;
        if (instrument instanceof AnnualizedAsianOption) {
            AnnualizedAsianOption instr = (AnnualizedAsianOption) instrument;
            BigDecimal exercisePrice = instr.strikeValue();
            notional = instr.notionalWithParticipation();
            initialSpot = instr.initialSpot();
            OptionTypeEnum optionTypeEnum = instr.optionType();
            switch (optionTypeEnum) {
                case PUT://K-S
                    spread = exercisePrice.subtract(underlyerPrice);
                    break;
                case CALL://S-K
                    spread = underlyerPrice.subtract(exercisePrice);
                    break;
            }
        }
        if (initialSpot.compareTo(BigDecimal.ZERO) == 0){
            return BigDecimal.ZERO;
        }
        // lot * multiplier * max(S-K, 0)或者 notional / initialSpot * max(S-K, 0)
        BigDecimal maxAmount = maxValue(spread, BigDecimal.ZERO);
        return notional.divide(initialSpot, 8, BigDecimal.ROUND_DOWN).multiply(maxAmount);

    }

    private BigDecimal maxValue(BigDecimal param1, BigDecimal param2) {
        return param1.compareTo(param2) > 0 ? param1 : param2;
    }
}
