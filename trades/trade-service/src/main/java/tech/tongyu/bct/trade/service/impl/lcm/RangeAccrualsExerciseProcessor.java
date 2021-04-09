package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedRangeAccrualsOption;
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
import java.util.*;

@Component
public class RangeAccrualsExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public RangeAccrualsExerciseProcessor(LCMEventRepo lcmEventRepo) {
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
        checkPositionStatus(position);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        InstrumentOfValue instrument = position.asset.instrumentOfValue();

        BigDecimal settleAmount = calRangeAccrualsExercisePrice(instrument, eventDetail);
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
            throw new CustomException("请输入区间累积结算金额settleAmount");
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
            throw new IllegalArgumentException(String.format("持仓编号:(%s),当前持仓已经结算，不能再次结算", position.positionId));
        }
        if (!(position.asset() instanceof AssetImpl)) {
            throw new IllegalArgumentException(
                    String.format("ExerciseProcessor只支持EquityAsset与Position(%s)的Asset类型（）不支持",
                            position.positionId, position.asset().getClass()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedRangeAccrualsOption)) {
            throw new IllegalArgumentException(
                    String.format("ExerciseProcessor只支持AsianOption与Position(%s)的IOV类型（）不支持",
                            position.positionId, position.asset().instrumentOfValue().getClass()));

        }
        if (instrument instanceof OptionExerciseFeature) {
            LocalDate nowDate = LocalDate.now();
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                throw new CustomException(
                        String.format("持仓编号:[%s],当前持仓为区间累积,还未到到期,到期日:[%s],不能进行结算操作",
                                position.positionId, expirationDate.toString()));
            }
        }
    }

    private BigDecimal calRangeAccrualsExercisePrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        AnnualizedRangeAccrualsOption iov = (AnnualizedRangeAccrualsOption) instrument;
        Map<LocalDate, BigDecimal> fixingObservations = iov.fixingObservations();
        BigDecimal lowBarrier = iov.lowBarrierValue();
        BigDecimal highBarrier = iov.highBarrierValue();

        Integer count = 0;
        int size = fixingObservations.size();
        for (Map.Entry<LocalDate, BigDecimal> entry : fixingObservations.entrySet()){
            if (Objects.isNull(entry.getValue())){
                throw new CustomException(String.format("观察日[%s]没有观察价格,请补全观察价格", entry.getKey().toString()));
            }
            if (isObservationPriceRight(entry.getValue(), lowBarrier, highBarrier)){
                count ++;
            }
        }

        BigDecimal payment = iov.paymentValue();
        return payment.multiply(BigDecimal.valueOf(count).divide(BigDecimal.valueOf(size), 10 , BigDecimal.ROUND_DOWN));
    }

    private Boolean isObservationPriceRight(BigDecimal observationPrice, BigDecimal lowBarrier, BigDecimal highBarrier){
        int lowResult = observationPrice.compareTo(lowBarrier);
        int highResult = observationPrice.compareTo(highBarrier);
        if (lowResult >= 0 && highResult <= 0){
            return true;
        }
        return false;
    }

}
