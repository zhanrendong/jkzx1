package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedAutoCallPhoenixOption;
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
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AutoCallPhoenixKnockOutProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private static final String KNOCK_OUT_DATE = "knockOutDate";

    LCMEventRepo lcmEventRepo;

    @Autowired
    public AutoCallPhoenixKnockOutProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.KNOCK_OUT;
    }

    @Override
    public boolean canPreSettle() {
        return true;
    }

    @Override
    public BigDecimal preSettle(BctTradePosition position, LCMEventDTO eventDto) {
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        BigDecimal settleAmount = calAutoCallPhoenixKnockOutPrice(instrument, eventDto.getEventDetail());
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();
        checkPositionStatus(position, eventDetail);

        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入凤凰敲出结算金额settleAmount");
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
            throw new CustomException(String.format("持仓编号[%s],当前持仓状态[%s],不能敲出",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedAutoCallPhoenixOption)) {
            throw new CustomException(String.format("AutoCallPhoenixProcessor只支持AutoCallPhoenixOption," +
                            "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }
        String knockOutDateStr = (String) eventDetail.get(KNOCK_OUT_DATE);
        if (StringUtils.isBlank(knockOutDateStr)){
            throw new CustomException("请输入敲出日期");
        }
        // 校验敲出日是否早于敲出日
        AnnualizedAutoCallPhoenixOption iov = (AnnualizedAutoCallPhoenixOption) instrument;
        LocalDate knockOutDate = LocalDate.parse(knockOutDateStr);
        LocalDate effectiveDate = iov.effectiveDate();
        if (knockOutDate.isBefore(effectiveDate)){
            throw new CustomException(String.format("敲出日期早于起始日,起始日:[%s],敲出日:[%s]",
                    effectiveDate.toString(), knockOutDateStr));
        }

    }

    private BigDecimal calAutoCallPhoenixKnockOutPrice(InstrumentOfValue instrument, Map<String, Object> eventDetail) {
        String knockOutDateStr = (String) eventDetail.get(KNOCK_OUT_DATE);
        if (StringUtils.isBlank(knockOutDateStr)){
            throw new CustomException("请输入敲出日期");
        }
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)){
            throw new CustomException("请输入标的物价格");
        }
        AnnualizedAutoCallPhoenixOption iov = (AnnualizedAutoCallPhoenixOption) instrument;
        BigDecimal daysInYear = iov.daysInYear();
        if (BigDecimal.ZERO.compareTo(daysInYear) == 0){
            throw new CustomException("年度计息天数为0");
        }

        LocalDate knockOutDate = LocalDate.parse(knockOutDateStr);
        BigDecimal couponPayment = iov.couponPayment();
        BigDecimal couponBarrier = iov.couponBarrierValue();
        KnockDirectionEnum knockDirection = iov.knockDirection();

        AtomicReference<LocalDate> lastObservationDate = new AtomicReference<>(iov.startDate());
        Map<LocalDate, BigDecimal> fixingObservations = iov.fixingObservations();
        return fixingObservations.entrySet()
                .stream()
                .filter(o -> !o.getKey().isAfter(knockOutDate))
                .map(o -> {
                    if (Objects.isNull(o.getValue())){
                        lastObservationDate.set(o.getKey());
                        return BigDecimal.ZERO;
                    }
                    BigDecimal notional = iov.notionalAmountFaceValue();
                    BigDecimal period = BigDecimal.valueOf(lastObservationDate.get().until(o.getKey(), ChronoUnit.DAYS));

                    lastObservationDate.set(o.getKey());
                    int barrierResult = o.getValue().compareTo(couponBarrier);
                    if (KnockDirectionEnum.UP.equals(knockDirection) && barrierResult >= 0) {
                        return calObservationPeriodPayment(period, daysInYear, notional, couponPayment);
                    } else if (KnockDirectionEnum.DOWN.equals(knockDirection) && barrierResult <= 0) {
                        return calObservationPeriodPayment(period, daysInYear, notional, couponPayment);
                    } else {
                        return BigDecimal.ZERO;
                    }
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    private BigDecimal calObservationPeriodPayment(BigDecimal period, BigDecimal daysInYear,
                                                   BigDecimal notional, BigDecimal coupon){
        return period.divide(daysInYear, 10, BigDecimal.ROUND_DOWN).multiply(notional).multiply(coupon);
    }


}
