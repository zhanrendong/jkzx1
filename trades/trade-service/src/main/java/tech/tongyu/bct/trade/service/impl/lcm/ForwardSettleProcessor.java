package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.SingleStrikeFeature;
import tech.tongyu.bct.cm.product.iov.impl.Forward;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ForwardSettleProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public ForwardSettleProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.SETTLE;
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        return calSettlementAmount(position, eventDto);
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
            throw new CustomException("请输入远期结算金额settleAmount");
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
        BctTradePosition bctTradePosition = new BctTradePosition();
        BeanUtils.copyProperties(position, bctTradePosition);
        return Arrays.asList(bctTradePosition);
    }

    private void checkPositionStatus(BctTradePosition position) {
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能结算",
                    position.positionId, position.lcmEventType.description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof Forward)) {
            throw new IllegalArgumentException(
                    String.format("ForwardSettleProcessor只支持Forward,与Position[%s]的结构类型[%s]不匹配",
                            position.positionId, position.asset().instrumentOfValue().getClass()));
        }
        if (instrument instanceof OptionExerciseFeature) {
            LocalDate nowDate = LocalDate.now();
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                throw new CustomException(
                        String.format("持仓编号:[%s],当前持仓为远期,还未到到期,到期日:[%s],不能进行行权操作",
                                position.positionId, expirationDate.toString()));
            }
        }
    }


    private BigDecimal calSettlementAmount(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)) {
            throw new IllegalArgumentException("请输入标的物价格");
        }
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();

        BigDecimal strike = BigDecimal.ZERO;
        if (instrument instanceof SingleStrikeFeature) {
            strike = ((SingleStrikeFeature) instrument).strikeValue();
        }

        BigDecimal settleAmount = BigDecimal.ZERO;
        switch (position.partyRole()) {
            case BUYER://S-K
                settleAmount = underlyerPrice.subtract(strike);
                break;
            case SELLER://K-S
                settleAmount = strike.subtract(underlyerPrice);
                break;
        }

        return settleAmount;
    }

}
