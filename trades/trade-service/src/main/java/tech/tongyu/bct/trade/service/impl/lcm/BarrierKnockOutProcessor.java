package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.product.iov.RebateTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.*;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTrade;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.QuoteDTO;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.trade.dao.dbo.LCMEvent;
import tech.tongyu.bct.trade.dao.repo.LCMEventRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMKnockOutNotificationDTO;
import tech.tongyu.bct.trade.dto.lcm.LCMNotificationInfoDTO;
import tech.tongyu.bct.trade.service.LCMProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class BarrierKnockOutProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private static final String UNDERLYER_PRICE = "underlyerPrice";

    LCMEventRepo lcmEventRepo;

    MarketDataService marketDataService;

    @Autowired
    public BarrierKnockOutProcessor(LCMEventRepo lcmEventRepo,
                                    MarketDataService marketDataService) {
        this.lcmEventRepo = lcmEventRepo;
        this.marketDataService = marketDataService;
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
        checkPositionStatus(position);
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        BigDecimal settleAmount = BigDecimal.ZERO;
        if (instrument instanceof BarrierFeature) {
            if (RebateTypeEnum.PAY_NONE.equals(((RebateFeature) instrument).rebateType())) {
                return BigDecimal.ZERO;
            }
            settleAmount = ((RebateFeature) instrument).rebateValue();
        }
        if (instrument instanceof DoubleSharkFinFeature){
            Map<String, Object> eventDetail = eventDto.getEventDetail();
            settleAmount = knockOutDoubleSharkFin(instrument, eventDetail);
        }
        return getCashFlowValueByPartyRole(settleAmount, position.partyRole());
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        Map<String, Object> eventDetail = eventDto.getEventDetail();
        String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
        if (StringUtils.isBlank(settleAmountStr)){
            throw new CustomException("请输入障碍敲出结算金额");
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

    @Override
    public List<LCMNotificationInfoDTO> notifications(Asset<InstrumentOfValue> asset) {
        InstrumentOfValue iov = asset.instrumentOfValue();
        Map<String, InstrumentOfValuePartyRoleTypeEnum> partyRoleTypeEnumMap = asset.legalPartyRoles().stream()
                .collect(Collectors.toMap(key -> key.party().partyCode(), value -> value.roleType()));
        if (iov instanceof OptionExerciseFeature) {
            Boolean notified = false;
            String instrumentId = null;
            BigDecimal nowPrice = BigDecimal.ZERO;
            LocalDate expirationDate = ((OptionExerciseFeature) iov).absoluteExpirationDate();
            LocalDate settlementDate = ((SettlementFeature) iov).settlementDate();
            SettlementTypeEnum settlementType = ((SettlementFeature) iov).settlementType();
            BigDecimal barrier = null;
            BigDecimal lowBarrier = null;
            BigDecimal highBarrier = null;
            if (iov instanceof BarrierFeature) {
                instrumentId = ((BarrierFeature) iov).underlyer().instrumentId();
                nowPrice = getLatestInstrumentPrice(instrumentId);
                KnockDirectionEnum knockDirection = ((BarrierFeature) iov).knockDirection();
                barrier = ((BarrierFeature) iov).barrierValue();
                notified = isPriceCloseToKnockOut(knockDirection, nowPrice, barrier);
            }
            if (iov instanceof DoubleSharkFinFeature) {
                instrumentId = ((DoubleSharkFinFeature) iov).underlyer().instrumentId();
                nowPrice = getLatestInstrumentPrice(instrumentId);
                lowBarrier = ((DoubleSharkFinFeature) iov).lowBarrierValue();
                highBarrier = ((DoubleSharkFinFeature) iov).highBarrierValue();
                notified = isPriceCloseToKnockOut(KnockDirectionEnum.DOWN, nowPrice, lowBarrier)
                        || isPriceCloseToKnockOut(KnockDirectionEnum.UP, nowPrice, highBarrier);
            }
            if (notified) {
                LCMKnockOutNotificationDTO notificationDTO = new LCMKnockOutNotificationDTO(
                        partyRoleTypeEnumMap,
                        LCMEventTypeEnum.KNOCK_OUT,
                        iov.productType(),
                        instrumentId,
                        nowPrice,
                        LocalDateTime.now()
                );

                notificationDTO.lowBarrier = lowBarrier;
                notificationDTO.highBarrier = highBarrier;
                notificationDTO.barrier = barrier;
                return Collections.singletonList(notificationDTO);
            }
        }
        return new ArrayList<>();
    }

    private void checkPositionStatus(BctTradePosition position){
        if (isStatusError(position.lcmEventType)) {
            throw new IllegalArgumentException(String.format("持仓编号[%s],当前持仓状态[%s],不能进行敲出操作",
                    position.positionId, position.getLcmEventType().description()));
        }
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        if (!(instrument instanceof BarrierFeature || instrument instanceof DoubleSharkFinFeature)) {
            throw new CustomException(String.format("BarrierKnockOutProcessor只支持BarrierFeature和DoubleSharkFinFeature,"
                    + "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }
        // 校验敲出日是否早于起始日
        LocalDate knockOutDate = LocalDate.now();
        LocalDate effectiveDate = ((EffectiveDateFeature) instrument).effectiveDate();
        if (knockOutDate.isBefore(effectiveDate)){
            throw new CustomException(String.format("敲出日期早于起始日,起始日:[%s],敲出日:[%s]",
                    effectiveDate.toString(), knockOutDate.toString()));
        }
    }

    private BigDecimal knockOutDoubleSharkFin(InstrumentOfValue instrument, Map<String, Object> eventDetail){
        String underlyerPriceStr = (String) eventDetail.get(UNDERLYER_PRICE);
        if (StringUtils.isBlank(underlyerPriceStr)) {
            throw new IllegalArgumentException("请输入标的物价格");
        }
        BigDecimal cashFlow = BigDecimal.ZERO;
        BigDecimal underlyerPrice = new BigDecimal(underlyerPriceStr);

        BigDecimal lowBarrier = ((DoubleSharkFinFeature) instrument).lowBarrierValue();
        BigDecimal highBarrier = ((DoubleSharkFinFeature) instrument).highBarrierValue();
        if (underlyerPrice.compareTo(lowBarrier) < 0){
            cashFlow = ((DoubleSharkFinFeature) instrument).lowRebateValue();
        }
        if (underlyerPrice.compareTo(highBarrier) > 0){
            cashFlow = ((DoubleSharkFinFeature) instrument).highRebateValue();
        }
        return cashFlow;
    }


    private Boolean isPriceCloseToKnockOut(KnockDirectionEnum knockDirection, BigDecimal nowPrice, BigDecimal barrier){
        BigDecimal ninetyValue = barrier.multiply(BigDecimal.valueOf(0.9));
        int compareResult = nowPrice.compareTo(ninetyValue);
        switch (knockDirection){
            case UP: // 现在价格或 >= 90%障碍价
                return compareResult >= 0;
            case DOWN: // 现在价格 <= 90%障碍价
                return compareResult <= 0;
        }
        return false;
    }

    private BigDecimal getLatestInstrumentPrice(String instrumentId){
        QuoteDTO quoteDto = marketDataService.listLatestQuotes(Arrays.asList(instrumentId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(String.format("获取标的物现价失败,instrumentId:%s", instrumentId)));
        Double nowPrice = quoteDto.getFields().get(QuoteFieldEnum.LAST);
        return BigDecimal.valueOf(nowPrice);
    }

    private LocalDateTime resolveDateTime(LocalDate expirationDate) {
        return LocalDateTime.of(expirationDate, LocalTime.MIDNIGHT);
    }
}
