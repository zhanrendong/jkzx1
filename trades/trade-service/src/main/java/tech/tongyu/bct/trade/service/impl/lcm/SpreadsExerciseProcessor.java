package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.BasketInstrumentConstituent;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedSpreadsOption;
import tech.tongyu.bct.cm.product.iov.impl.SpreadsInstrument;
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
public class SpreadsExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    LCMEventRepo lcmEventRepo;

    @Autowired
    public SpreadsExerciseProcessor(LCMEventRepo lcmEventRepo) {
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
        BigDecimal settleAmount = calSpreadExercisePrice(instrument, eventDetail);
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
        if (StringUtils.isBlank(settleAmountStr)) {
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

    private void checkPositionStatus(BctTradePosition position) {
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号:(%s),当前持仓已经行权，不能再次行权", position.positionId));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedSpreadsOption)) {
            throw new IllegalArgumentException(
                    String.format("SpreadsExerciseProcessor只支持SpreadsOption,与Position[%s]的结构类型[%s]不匹配",
                            position.positionId, position.asset().instrumentOfValue().getClass()));

        }

        checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);

    }

    private BigDecimal calSpreadExercisePrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        String underlyerPriceStr1 = (String) eventDetail.get(UNDERLYER_PRICE + "1");
        String underlyerPriceStr2 = (String) eventDetail.get(UNDERLYER_PRICE + "2");
        if (StringUtils.isBlank(underlyerPriceStr1)) {
            throw new IllegalArgumentException("请输入标的物1价格");
        }
        if (StringUtils.isBlank(underlyerPriceStr2)) {
            throw new IllegalArgumentException("请输入标的物2价格");
        }
        BigDecimal notional = BigDecimal.ZERO;
        BigDecimal spread = BigDecimal.ZERO;
        if (instrument instanceof AnnualizedSpreadsOption) {
            AnnualizedSpreadsOption instr = (AnnualizedSpreadsOption) instrument;
            notional = instr.notionalWithParticipation();
            BigDecimal underlyerPrice1 = new BigDecimal(underlyerPriceStr1);
            BigDecimal underlyerPrice2 = new BigDecimal(underlyerPriceStr2);
            spread = clacSpreadPercent(instr, underlyerPrice1, underlyerPrice2);
        }
        BigDecimal maxAmount = maxValue(spread, BigDecimal.ZERO);
        return maxAmount.multiply(notional);

    }


    private BigDecimal maxValue(BigDecimal param1, BigDecimal param2) {
        return param1.compareTo(param2) > 0 ? param1 : param2;
    }

    private BigDecimal clacSpreadPercent(AnnualizedSpreadsOption option, BigDecimal underlyerPrice1, BigDecimal underlyerPrice2) {
        List<BasketInstrumentConstituent> constituents = ((SpreadsInstrument) option.underlyer()).constituents();
        BigDecimal initialSpot1 = constituents.get(0).initialSpot();
        BigDecimal initialSpot2 = constituents.get(1).initialSpot();
        BigDecimal weight1 = constituents.get(0).weight();
        BigDecimal weight2 = constituents.get(1).weight();

        OptionTypeEnum optionType = option.optionType();
        ProductTypeEnum productType = option.productType();
        BigDecimal exercisePercent = BigDecimal.ZERO;

        if (productType == ProductTypeEnum.SPREAD_EUROPEAN) {
            if (isZero(initialSpot1) || isZero(initialSpot2)) {
                throw new IllegalArgumentException("pricing error：存在期初价格为0的标的物");
            }
            exercisePercent = underlyerPrice1.divide(initialSpot1, 8, BigDecimal.ROUND_DOWN).multiply(weight1)
                    .subtract(underlyerPrice2.divide(initialSpot2, 8, BigDecimal.ROUND_DOWN).multiply(weight2));
        } else if (productType == ProductTypeEnum.RATIO_SPREAD_EUROPEAN) {
            if (isZero(underlyerPrice2)) {
                throw new IllegalArgumentException("pricing error：标的物2价格为0");
            }
            exercisePercent = underlyerPrice1.divide(underlyerPrice2, 8, BigDecimal.ROUND_DOWN);
        }

        BigDecimal strike = option.strikePercentValue();
        BigDecimal spread = BigDecimal.ZERO;

        switch (optionType) {
            case CALL:
                spread = exercisePercent.subtract(strike);
                break;
            case PUT:
                spread = strike.subtract(exercisePercent);
                break;
        }
        return spread;
    }

    private boolean isZero(BigDecimal num) {
        return num.compareTo(BigDecimal.ZERO) == 0;
    }
}
