package tech.tongyu.bct.trade.service.impl.lcm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedConcavaConvexOption;
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
public class ConcaveConvexOptionExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public ConcaveConvexOptionExerciseProcessor(LCMEventRepo lcmEventRepo){
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

    @Override
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if(instrument instanceof AnnualizedConcavaConvexOption) {
            AnnualizedConcavaConvexOption option = (AnnualizedConcavaConvexOption) instrument;
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException(String.format("请输入二元%s结算金额settleAmount", option.concavaed ? "凹式": "凸式"));
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
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        BigDecimal settleAmount = concaveConvexExercisePrice(instrument, eventDto.getEventDetail());
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    public boolean isStatusError(LCMEventTypeEnum nowStatus) {
        return CollectionUtils.contains(Lists.newArrayList(
                LCMEventTypeEnum.UNWIND,
                LCMEventTypeEnum.EXERCISE,
                LCMEventTypeEnum.EXPIRATION
        ), nowStatus);
    }

    private void checkPositionStatus(BctTradePosition position, Map<String, Object> eventDetail){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行权操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedConcavaConvexOption)) {
            throw new CustomException(String.format("ConcavaConvexOptionExerciseProcessor只支持二元凹式/凸式期权行权结算,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }

        checkEuropeanOptionExpirationDate(position.getPositionId(), instrument);
    }

    private BigDecimal concaveConvexExercisePrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)) {
            throw new IllegalArgumentException("请输入标的物价格");
        }
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);
        if (instrument instanceof AnnualizedConcavaConvexOption) {
            AnnualizedConcavaConvexOption instr = (AnnualizedConcavaConvexOption) instrument;
            BigDecimal initialSpot = instr.initialSpot();
            BigDecimal highBarrier = instr.highBarrierValue();
            BigDecimal lowBarrier = instr.lowBarrierValue();

            if (initialSpot.compareTo(BigDecimal.ZERO) == 0){
                return BigDecimal.ZERO;
            }

            boolean inRange = highBarrier.compareTo(underlyerPrice) >= 0
                    && lowBarrier.compareTo(underlyerPrice) <= 0;

            return instr.concavaed
                    ? inRange ? BigDecimal.ZERO : instr.paymentValue()
                    : inRange ? instr.paymentValue() : BigDecimal.ZERO;
        }
        else{
            throw new CustomException("期权类型转换错误");
        }
    }

}
