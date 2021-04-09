package tech.tongyu.bct.trade.service.impl.lcm;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.asset.AssetImpl;
import tech.tongyu.bct.cm.product.iov.AutoCallPaymentTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedAutoCallOption;
import tech.tongyu.bct.cm.product.iov.impl.AnnualizedVanillaOption;
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
public class AutoCallExpirationExerciseProcessor extends ExerciseProcessorCommon implements LCMProcessor {

    private LCMEventRepo lcmEventRepo;

    private VanillaExerciseProcessor vanillaExerciseProcessor;

    @Autowired
    public AutoCallExpirationExerciseProcessor(LCMEventRepo lcmEventRepo,
                                               VanillaExerciseProcessor vanillaExerciseProcessor) {
        this.lcmEventRepo = lcmEventRepo;
        this.vanillaExerciseProcessor = vanillaExerciseProcessor;
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
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        AnnualizedAutoCallOption iov = (AnnualizedAutoCallOption) instrument;

        AutoCallPaymentTypeEnum paymentType = iov.autoCallPaymentType;
        if (AutoCallPaymentTypeEnum.FIXED.equals(paymentType)){
            BigDecimal fixedPayment = iov.fixedPaymentValue();
            return getCashFlowValueByPartyRole(fixedPayment, position.partyRole());
        }
        return calVanillaExercisePrice(null, position, eventDto, true);
    }

    @Override
    @Transactional
    public List<BctTradePosition> process(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto) {
        checkPositionStatus(position);
        InstrumentOfValue instrument = position.asset.instrumentOfValue();
        AnnualizedAutoCallOption iov = (AnnualizedAutoCallOption) instrument;
        AutoCallPaymentTypeEnum paymentType = iov.autoCallPaymentType;
        if (AutoCallPaymentTypeEnum.FIXED.equals(paymentType)){
            Map<String, Object> eventDetail = eventDto.getEventDetail();
            String settleAmountStr = (String) eventDetail.get(SETTLE_AMOUNT);
            if (StringUtils.isBlank(settleAmountStr)){
                throw new CustomException("请输入雪球到期固定结算金额settleAmount");
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
        }else{
            calVanillaExercisePrice(trade, position, eventDto, false);
        }

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
        if (!(instrument instanceof AnnualizedAutoCallOption)) {
            throw new CustomException(String.format("AutoCallExpirationExerciseProcessor只支持AutoCallOption," +
                            "与Position[%s]的结构类型[%s]不匹配", position.getPositionId(), instrument.getClass()));
        }
        if (instrument instanceof OptionExerciseFeature) {
            LocalDate nowDate = LocalDate.now();
            LocalDate expirationDate = ((OptionExerciseFeature) instrument).absoluteExpirationDate();
            if (nowDate.isBefore(expirationDate)) {
                throw new CustomException(
                        String.format("持仓编号[%s],当前持仓为autoCall雪球,还未到到期,到期日[%s],不能进行到期结算操作",
                                position.positionId, expirationDate.toString()));
            }
        }
    }

    private BigDecimal calVanillaExercisePrice(BctTrade trade, BctTradePosition position, LCMEventDTO eventDto,
                                               Boolean isPreSettle) {
        eventDto.setLcmEventType(LCMEventTypeEnum.EXERCISE);
        InstrumentOfValue instrument = position.asset().instrumentOfValue();
        AnnualizedAutoCallOption iov = (AnnualizedAutoCallOption) instrument;
        AutoCallPaymentTypeEnum paymentType = iov.autoCallPaymentType;
        AssetImpl asset = (AssetImpl) position.getAsset();
        AnnualizedVanillaOption vanillaOption = new AnnualizedVanillaOption(iov.underlyer, iov.underlyerMultiplier,
                OptionTypeEnum.valueOf(paymentType.name()), ProductTypeEnum.VANILLA_EUROPEAN, iov.assetClassType,
                iov.notionalAmount, iov.participationRate, iov.startDate, iov.endDate, iov.daysInYear, iov.term,
                iov.annualized, iov.exerciseType, iov.expirationDate, iov.expirationTime, iov.initialSpot,
                iov.settlementDate, iov.settlementType, iov.autoCallStrike, iov.effectiveDate, iov.specifiedPrice,
                iov.frontPremium, iov.minimumPremium, iov.premium, iov.annValRatio);
        AssetImpl vanillaAsset = new AssetImpl(
                vanillaOption,
                asset.partyRoles,
                asset.quantityFactors);
        BctTradePosition vanillaEuropeanPosition = new BctTradePosition();
        vanillaEuropeanPosition.setAsset(vanillaAsset);
        vanillaEuropeanPosition.setLcmEventType(LCMEventTypeEnum.OPEN);
        vanillaEuropeanPosition.setPositionId(position.getPositionId());
        vanillaEuropeanPosition.setCounterparty(position.getCounterparty());
        vanillaEuropeanPosition.setCounterpartyAccount(position.getCounterpartyAccount());
        if (isPreSettle){
            return vanillaExerciseProcessor.preSettle(vanillaEuropeanPosition, eventDto);
        }
        vanillaExerciseProcessor.process(trade, vanillaEuropeanPosition, eventDto);
        return BigDecimal.ZERO;

    }
}
