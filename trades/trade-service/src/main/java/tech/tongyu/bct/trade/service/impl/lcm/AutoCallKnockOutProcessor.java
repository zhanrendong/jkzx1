package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedAutoCallOption;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class AutoCallKnockOutProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private static final String KNOCK_OUT_DATE = "knockOutDate";

    @Autowired
    public AutoCallKnockOutProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.KNOCK_OUT;
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position, eventDto.getEventDetail());
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        BigDecimal settleAmount = calAutoCallKnockOutPrice(instrument, eventDetail);
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入雪球敲出结算金额settleAmount");
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

    private void checkPositionStatus(BctTradePosition position,  Map<String, Object> eventDetail){
        if (isStatusError(position.lcmEventType)) {
            throw new CustomException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行敲出操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedAutoCallOption)) {
            throw new CustomException(String.format("AutoCallKnockOutProcessor只支持AutoCallOption," +
                            "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }
        String knockOutDateStr = (String) eventDetail.get(KNOCK_OUT_DATE);
        if (StringUtils.isBlank(knockOutDateStr)){
            throw new CustomException("请输入敲出日期knockOutDate");
        }
        // 校验敲出日是否早于敲出日
        AnnualizedAutoCallOption iov = (AnnualizedAutoCallOption) instrument;
        LocalDate knockOutDate = LocalDate.parse(knockOutDateStr);
        LocalDate effectiveDate = iov.effectiveDate();
        if (knockOutDate.isBefore(effectiveDate)){
            throw new CustomException(String.format("敲出日期早于起始日,起始日:[%s],敲出日:[%s]",
                    effectiveDate.toString(), knockOutDateStr));
        }

    }

    private BigDecimal calAutoCallKnockOutPrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        String knockOutDateStr = (String) eventDetail.get(KNOCK_OUT_DATE);
        if (StringUtils.isBlank(knockOutDateStr)){
            throw new CustomException("请输入敲出日期");
        }
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)){
            throw new CustomException("请输入标的物价格");
        }
        LocalDate knockOutDate = LocalDate.parse(knockOutDateStr);
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);

        AnnualizedAutoCallOption iov = (AnnualizedAutoCallOption) instrument;
//        // 校验敲出日是否能够敲出
//        BigDecimal observationPrice;
//        Map<LocalDate, BigDecimal> fixingObservations = iov.fixingObservations();
//        if (fixingObservations.containsKey(knockOutDate)){
//            observationPrice = fixingObservations.get(knockOutDate);
//        } else {
//            LocalDate nearestObservationDate = getNearestObservationDateByKnockOutDate(knockOutDate,
//                    new ArrayList<>(fixingObservations.keySet()));
//            if (Objects.isNull(nearestObservationDate)){
//                throw new CustomException(String.format("敲出日:[%s],不符合任何观察日条件", knockOutDateStr));
//            }
//            observationPrice = fixingObservations.get(nearestObservationDate);
//        }
//        // 校验标的物价格是否达到观察价格,是否符合敲出价格
//        if (Objects.isNull(observationPrice)){
//            throw new CustomException(String.format("敲出日:[%s]没有观察价格", knockOutDate.toString()));
//        }
//        if (underlyerPrice.compareTo(observationPrice) < 0){
//            throw new CustomException(String.format("标的物价格未达到观察价格,不能执行敲出操作",
//                    underlyerPriceStr, observationPrice.toPlainString()));
//        }
        LocalDate effectiveDate = iov.effectiveDate();
        BigDecimal interval = BigDecimal.valueOf(effectiveDate.until(knockOutDate, ChronoUnit.DAYS));
        BigDecimal notional = iov.notionalAmountFaceValue().multiply(iov.participationRate());
        BigDecimal daysInYear = iov.daysInYear();
        BigDecimal coupon = iov.couponPayment();
        if (BigDecimal.ZERO.compareTo(daysInYear) == 0){
            throw new CustomException("年度计息天数为0");
        }
        //名义本金*coupon（%）*（敲出日期-起始日）/年度计息天数
        return notional.multiply(coupon).multiply(interval).divide(daysInYear, 10, BigDecimal.ROUND_DOWN);
    }

    private LocalDate getNearestObservationDateByKnockOutDate(LocalDate knockOutDate, List<LocalDate> observationDates){
        Collections.sort(observationDates);
        LocalDate nearestObservationDate = null;
        for (LocalDate observationDate: observationDates) {
            if (observationDate.isBefore(knockOutDate)){
                nearestObservationDate = observationDate;
            }
            if (observationDate.isAfter(knockOutDate)){
                break;
            }
        }
        return nearestObservationDate;
    }
}
