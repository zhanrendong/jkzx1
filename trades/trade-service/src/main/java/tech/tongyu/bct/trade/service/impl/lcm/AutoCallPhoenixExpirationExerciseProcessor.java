package tech.tongyu.bct.trade.service.impl.lcm;

import io.vavr.Tuple3;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
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

@Component
public class AutoCallPhoenixExpirationExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    @Autowired
    public AutoCallPhoenixExpirationExerciseProcessor(LCMEventRepo lcmEventRepo) {
        this.lcmEventRepo = lcmEventRepo;
    }

    @Override
    public LCMEventTypeEnum eventType() {
        return LCMEventTypeEnum.SETTLE;
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
        checkPositionStatus(position);
        BigDecimal settleAmount = calPhoenixExercisePrice(position, eventDto);
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入凤凰到期结算金额settleAmount");
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
            throw new CustomException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行到期结算操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof AnnualizedAutoCallPhoenixOption)) {
            throw new CustomException(String.format("AutoCallPhoenixExpirationExerciseProcessor只支持AutoCallPhoenixOption,"
                            + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }
        if (instrument instanceof OptionExerciseFeature) {
            LocalDate nowDate = LocalDate.now();
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                throw new CustomException(
                        String.format("持仓编号[%s],当前持仓为autoCall凤凰式,还未到到期,到期日[%s],不能进行到期结算操作",
                                position.positionId, expirationDate.toString()));
            }
        }
    }

    private BigDecimal calPhoenixExercisePrice(BctTradePosition position, LCMEventDTO eventDto) {

        Map<String, Object> eventDetail = eventDto.getEventDetail();

        InstrumentOfValue instrument = position.getAsset().instrumentOfValue();
        AnnualizedAutoCallPhoenixOption iov = (AnnualizedAutoCallPhoenixOption) instrument;

        List<Tuple3<LocalDate, BigDecimal, BigDecimal>> fixingCouponPayments = iov.fixingCouponPayments();
        KnockDirectionEnum knockDirection = iov.knockDirection();
        BigDecimal couponBarrier = iov.couponBarrierValue();
        BigDecimal couponPayments = fixingCouponPayments.stream()
                .map(v->{
                    BigDecimal fixing = v._2;
                    BigDecimal couponPayment = v._3;
                    switch (knockDirection){
                        case UP:
                            if (Objects.isNull(fixing) || fixing.compareTo(couponBarrier) <= 0){
                                return BigDecimal.ZERO;
                            }
                            break;
                        case DOWN:
                            if (Objects.isNull(fixing) || fixing.compareTo(couponBarrier) >= 0){
                                return BigDecimal.ZERO;
                            }
                            break;
                    }
                    return couponPayment;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expirationPayment = BigDecimal.ZERO;
        Boolean isKnockIn = iov.knockedIn();
        if (Objects.nonNull(isKnockIn) && isKnockIn){
            String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
            if (StringUtils.isBlank(underlyerPriceStr)){
                throw new CustomException("请输入标的物价格");
            }
            BigDecimal notional = iov.notionalAmountFaceValue().multiply(iov.participationRate());
            BigDecimal daysInYear = iov.daysInYear();
            BigDecimal initialSpot = iov.initialSpot();
            if (BigDecimal.ZERO.compareTo(daysInYear) != 0 && BigDecimal.ZERO.compareTo(initialSpot) != 0){
                LocalDate effectiveDate = iov.effectiveDate();
                LocalDate expirationDate = iov.absoluteExpirationDate();
                BigDecimal interval = BigDecimal.valueOf(effectiveDate.until(expirationDate, ChronoUnit.DAYS));

                BigDecimal knockInStrikeValue = iov.knockInStrikeValue();
                BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);

                BigDecimal spread = BigDecimal.ZERO;
                switch (iov.knockInOptionType()){
                    case PUT:
                        spread = knockInStrikeValue.subtract(underlyerPrice);
                        break;
                    case CALL:
                        spread = underlyerPrice.subtract(knockInStrikeValue);
                        break;
                }
                BigDecimal maxValue = maxValue(spread, BigDecimal.ZERO);
                expirationPayment = notional.divide(initialSpot, 10 , BigDecimal.ROUND_DOWN).multiply(interval)
                        .divide(daysInYear, 10, BigDecimal.ROUND_DOWN).multiply(maxValue);
            }
        }
        return couponPayments.subtract(expirationPayment);

    }

    private BigDecimal maxValue(BigDecimal param1, BigDecimal param2) {
        return param1.compareTo(param2) > 0 ? param1 : param2;
    }
}
