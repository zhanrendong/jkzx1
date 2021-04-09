package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedDoubleTouchOption;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
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
public class DoubleOneTouchOrNoTouchExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public DoubleOneTouchOrNoTouchExerciseProcessor(LCMEventRepo lcmEventRepo){
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return Lists.newArrayList();
    }

    @Override
    public boolean isStatusError(LCMEventTypeEnum nowStatus) {
        return CollectionUtils.contains(Lists.newArrayList(
                LCMEventTypeEnum.UNWIND,
                LCMEventTypeEnum.EXERCISE,
                LCMEventTypeEnum.EXPIRATION,
                LCMEventTypeEnum.SETTLE,
                LCMEventTypeEnum.KNOCK_OUT
        ), nowStatus);
    }

    private void checkPositionStatus(BctTradePosition position, Map<String, Object> eventDTO){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行结算操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedDoubleTouchOption)) {
            throw new CustomException(String.format("AnnualizedDoubleTouchOption只支持美式双触碰/双不触碰期权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }
    }

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedDoubleTouchOption) {
            AnnualizedDoubleTouchOption option = (AnnualizedDoubleTouchOption) instrument;
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException(String.format("请输入美式双%s触碰结算金额settleAmount", option.touched ? "": "不"));
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
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.SETTLE;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        BigDecimal settleAmount = calSettlePayment(instrument, eventDto.getEventDetail());
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    private Boolean getInRange(InstrumentOfValue instrument, BigDecimal underlyerPrice){
        if (instrument instanceof AnnualizedDoubleTouchOption) {
            AnnualizedDoubleTouchOption instr = (AnnualizedDoubleTouchOption) instrument;
            return instr.highBarrierValue().compareTo(underlyerPrice) >= 0
                    && instr.lowBarrierValue().compareTo(underlyerPrice) <= 0;
        }
        else {
            throw new CustomException("期权类型转换错误");
        }
    }

    private BigDecimal calSettlePayment(InstrumentOfValue instrument, Map<String, Object> eventDetail){
        BigDecimal underlyerPrice = getUnderlyerPrice(eventDetail);
        AnnualizedDoubleTouchOption instr = (AnnualizedDoubleTouchOption)instrument;
        if(instr.initialSpot.compareTo(BigDecimal.ZERO) == 0){
            //WARNING: should throw out exception here?
            return BigDecimal.ZERO;
        }

        return instr.touched
                ? calDoubleOneTouchSettlePayment(getInRange(instrument, underlyerPrice),instr)
                : calDoubleNoTouchSettlePayment(getInRange(instrument, underlyerPrice), instr);
    }

    private BigDecimal calDoubleOneTouchSettlePayment(Boolean inRange, AnnualizedDoubleTouchOption instrument) {
        return inRange
                 ? BigDecimal.ZERO
                 : instrument.rebateValue();
    }

    private BigDecimal calDoubleNoTouchSettlePayment(Boolean inRange, AnnualizedDoubleTouchOption instrument) {
        return inRange
                ? instrument.rebateValue()
                : BigDecimal.ZERO;
    }
}
