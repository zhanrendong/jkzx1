package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.*;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.Percent;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AnnualizedAutoCallPhoenixOption<U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        InitialSpotFeature, PremiumFeature, SettlementFeature, EffectiveDateFeature,  BarrierFeature, FixingFeature,
        AutoCallPaymentFeature, CouponFeature, KnockInFeature{

    public AnnualizedAutoCallPhoenixOption() {
    }

    public AnnualizedAutoCallPhoenixOption(U underlyer, ProductTypeEnum productType, InstrumentAssetClassTypeEnum assetClassType,
                                           BusinessCenterTimeTypeEnum specifiedPrice, ExerciseTypeEnum exerciseType,
                                           BigDecimal underlyerMultiplier, BigDecimal participationRate, BigDecimal initialSpot,
                                           UnitOfValue<BigDecimal> notionalAmount, UnitOfValue<BigDecimal> premium,
                                           UnitOfValue<BigDecimal> frontPremium, UnitOfValue<BigDecimal> minimumPremium,
                                           Boolean annualized, BigDecimal daysInYear, BigDecimal term,
                                           SettlementTypeEnum settlementType, LocalDate settlementDate,
                                           LocalDate startDate, LocalDate endDate, LocalDate effectiveDate,
                                           AdjustedDate expirationDate, LocalTime expirationTime,
                                           KnockDirectionEnum knockDirection, UnitOfValue<BigDecimal> barrier,
                                           String knockOutObservationStep, UnitOfValue<BigDecimal> couponBarrier,
                                           BigDecimal couponPayment, AutoCallPaymentTypeEnum autoCallPaymentType,
                                           Map<LocalDate, BigDecimal> fixingObservations, Map<LocalDate, LocalDate> fixingPaymentDates,
                                           Boolean knockedIn,
                                           LocalDate knockInDate, String knockInObservationStep, OptionTypeEnum knockInOptionType,
                                           UnitOfValue<BigDecimal> knockInStrike, UnitOfValue<BigDecimal> knockInBarrier,
                                           List<LocalDate> knockInObservationDates, BigDecimal annValRatio) {
        this.underlyer = underlyer;
        this.productType = productType;
        this.assetClassType = assetClassType;
        this.specifiedPrice = specifiedPrice;
        this.exerciseType = exerciseType;
        this.underlyerMultiplier = underlyerMultiplier;
        this.participationRate = participationRate;
        this.initialSpot = initialSpot;
        this.notionalAmount = notionalAmount;
        this.premium = premium;
        this.frontPremium = frontPremium;
        this.minimumPremium = minimumPremium;
        this.annualized = annualized;
        this.daysInYear = daysInYear;
        this.term = term;
        this.settlementType = settlementType;
        this.settlementDate = settlementDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.expirationTime = expirationTime;
        this.knockDirection = knockDirection;
        this.barrier = barrier;
        this.knockOutObservationStep = knockOutObservationStep;
        this.couponBarrier = couponBarrier;
        this.couponPayment = couponPayment;
        this.autoCallPaymentType = autoCallPaymentType;
        this.fixingObservations = fixingObservations;
        this.fixingPaymentDates = fixingPaymentDates;
        this.knockedIn = knockedIn;
        this.knockInDate = knockInDate;
        this.knockInObservationStep = knockInObservationStep;
        this.knockInOptionType = knockInOptionType;
        this.knockInStrike = knockInStrike;
        this.knockInBarrier = knockInBarrier;
        this.knockInObservationDates = knockInObservationDates;
        this.annValRatio = annValRatio;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public U underlyer;

    @Override
    public U underlyer() {
        return underlyer;
    }

    @Override
    public String instrumentId() {
        return String.format("AutoCallPhoenix_option_European_%s",
                underlyer().instrumentId());
    }

    public ProductTypeEnum productType;

    @Override
    public ProductTypeEnum productType() {
        return productType;
    }

    public InstrumentAssetClassTypeEnum assetClassType;

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return assetClassType;
    }

    public BusinessCenterTimeTypeEnum specifiedPrice;

    @Override
    public BusinessCenterTimeTypeEnum specifiedPrice() {
        return specifiedPrice;
    }

    public ExerciseTypeEnum exerciseType;

    @Override
    public ExerciseTypeEnum exerciseType() {
        return exerciseType;
    }

    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return Arrays.asList(InstrumentOfValuePartyRoleTypeEnum.BUYER, InstrumentOfValuePartyRoleTypeEnum.SELLER);
    }

    public BigDecimal underlyerMultiplier;

    @Override
    public BigDecimal underlyerMultiplier() {
        return underlyerMultiplier;
    }

    public BigDecimal participationRate;

    @Override
    public BigDecimal participationRate() {
        return participationRate;
    }

    public BigDecimal initialSpot;

    @Override
    public BigDecimal initialSpot() {
        return initialSpot;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return  new ArrayList<>();
    }

    public UnitOfValue<BigDecimal> notionalAmount;

    @Override
    public UnitOfValue<BigDecimal> notionalAmount() {
        return notionalAmount;
    }

    @Override
    public void notionalChangeLCMEvent(UnitOfValue<BigDecimal> notional) {
        this.notionalAmount = notional;
    }

    public UnitOfValue<BigDecimal> premium;

    @Override
    public UnitOfValue<BigDecimal> premium() {
        return premium;
    }

    public UnitOfValue<BigDecimal> frontPremium;

    @Override
    public UnitOfValue<BigDecimal> frontPremium() {
        return frontPremium;
    }

    public UnitOfValue<BigDecimal> minimumPremium;

    @Override
    public UnitOfValue<BigDecimal> minimumPremium() {
        return minimumPremium;
    }

    public Boolean annualized;

    @Override
    public Boolean isAnnualized() {
        return annualized;
    }

    public BigDecimal annValRatio;

    @Override
    public BigDecimal annValRatio() {
        return annValRatio;
    }

    public BigDecimal daysInYear;

    @Override
    public BigDecimal daysInYear() {
        return daysInYear;
    }

    public BigDecimal term;

    @Override
    public BigDecimal term() {
        return term;
    }

    public SettlementTypeEnum settlementType;

    @Override
    public SettlementTypeEnum settlementType() {
        return settlementType;
    }

    public LocalDate settlementDate;

    @Override
    public LocalDate settlementDate() {
        return settlementDate;
    }

    public LocalDate startDate;

    @Override
    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate;

    @Override
    public LocalDate endDate() {
        return endDate;
    }

    public LocalDate effectiveDate;

    @Override
    public LocalDate effectiveDate() {
        return effectiveDate;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public AdjustedDate expirationDate;

    @Override
    public AdjustedDate expirationDate() {
        return expirationDate;
    }

    @Override
    public void rollLCMEvent(LocalDate expirationDate) {
        this.expirationDate = new AbsoluteDate(expirationDate);
        this.settlementDate = expirationDate;
        this.endDate = expirationDate;
    }

    public LocalTime expirationTime;

    @Override
    public LocalTime expirationTime() {
        return expirationTime;
    }

    public KnockDirectionEnum knockDirection;

    @Override
    public KnockDirectionEnum knockDirection() {
        return knockDirection;
    }

    public UnitOfValue<BigDecimal> barrier;

    @Override
    public UnitOfValue<BigDecimal> barrier() {
        return barrier;
    }

    public String knockOutObservationStep;

    public String knockOutObservationStep(){
        return knockOutObservationStep;
    }

    @Override
    public BarrierObservationTypeEnum observationType() {
        return BarrierObservationTypeEnum.DISCRETE;
    }

    public UnitOfValue<BigDecimal> couponBarrier;

    @Override
    public UnitOfValue<BigDecimal> couponBarrier() {
        return couponBarrier;
    }

    public BigDecimal couponPayment;

    @Override
    public BigDecimal couponPayment() {
        return couponPayment;
    }

    public AutoCallPaymentTypeEnum autoCallPaymentType;

    @Override
    public AutoCallPaymentTypeEnum autoCallPaymentType() {
        return autoCallPaymentType;
    }

    public Map<LocalDate, BigDecimal> fixingObservations;

    @Override
    public Map<LocalDate, BigDecimal> fixingObservations() {
        return fixingObservations;
    }


    public Map<LocalDate, LocalDate> fixingPaymentDates;

    @Override
    public Map<LocalDate, LocalDate> fixingPaymentDates() {
        return fixingPaymentDates;
    }

    public List<BigDecimal> couponPaymentValues(){
        AtomicReference<LocalDate> lastObservationDate = new AtomicReference<>(startDate);
        return fixingObservations.entrySet()
                .stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(o -> {
                    BigDecimal period = BigDecimal.valueOf(lastObservationDate.get().until(o.getKey(), ChronoUnit.DAYS));
                    lastObservationDate.set(o.getKey());

                    return period.divide(daysInYear, 10, BigDecimal.ROUND_DOWN).
                                multiply(notionalAmountFaceValue()).multiply(this.couponPayment).multiply(participationRate);
                }).collect(Collectors.toList());
    }

    public List<Tuple3<LocalDate, BigDecimal, BigDecimal>> fixingCouponPayments(){
        AtomicReference<LocalDate> lastObservationDate = new AtomicReference<>(startDate);
        return fixingObservations.entrySet()
                .stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .map(o -> {
                    BigDecimal period = BigDecimal.valueOf(lastObservationDate.get().until(o.getKey(), ChronoUnit.DAYS));
                    lastObservationDate.set(o.getKey());

                    BigDecimal couponPayment = period.divide(daysInYear, 10, BigDecimal.ROUND_DOWN).
                            multiply(notionalAmountFaceValue()).multiply(this.couponPayment).multiply(participationRate);
                    return Tuple.of(o.getKey(), o.getValue(), couponPayment);
                }).collect(Collectors.toList());

    }

    @Override
    public void observationLCMEvent(LocalDate observationDate, BigDecimal observationPrice) {
        fixingObservations.put(observationDate, observationPrice);
    }

    public Boolean knockedIn;

    @Override
    public Boolean knockedIn() {
        return knockedIn;
    }

    public LocalDate knockInDate;

    @Override
    public LocalDate knockInDate() {
        return knockInDate;
    }

    @Override
    public void updateKnockInDate(LocalDate knockInDate) {
        this.knockedIn = Boolean.TRUE;
        this.knockInDate = knockInDate;
    }

    @Override
    public KnockDirectionEnum knockInDirection() {
        if (KnockDirectionEnum.UP.equals(knockDirection)){
            return KnockDirectionEnum.DOWN;
        }
        return KnockDirectionEnum.UP;
    }

    public String knockInObservationStep;

    public String knockInObservationStep() {
        return knockInObservationStep;
    }

    public OptionTypeEnum knockInOptionType;

    public OptionTypeEnum knockInOptionType() {
        return knockInOptionType;
    }

    public UnitOfValue<BigDecimal> knockInStrike;

    public UnitOfValue<BigDecimal> knockInStrike() {
        return knockInStrike;
    }

    public BigDecimal knockInStrikeValue(){
        if (knockInStrike().unit() instanceof Percent)
            return knockInStrike().value().multiply(initialSpot());
        else {
            return knockInStrike().value();
        }
    }

    public UnitOfValue<BigDecimal> knockInBarrier;

    @Override
    public UnitOfValue<BigDecimal> knockInBarrier() {
        return knockInBarrier;
    }

    public List<LocalDate> knockInObservationDates;

    public List<LocalDate> knockInObservationDates() {
        return knockInObservationDates;
    }

    @Override
    public InstrumentOfValue knockInInstrumentOfValue() {
        Map<LocalDate, Object> observationBarriers = fixingObservations.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, null));
        return new AnnualizedAutoCallOption(underlyer, ProductTypeEnum.AUTOCALL, assetClassType, specifiedPrice, exerciseType,
                underlyerMultiplier, participationRate, initialSpot, notionalAmount, premium, frontPremium, minimumPremium,
                annualized, daysInYear, term, settlementType, settlementDate, startDate, endDate, effectiveDate, expirationDate,
                expirationTime, fixingObservations, fixingPaymentDates, observationBarriers, knockInDirection(), barrier,
                BigDecimal.ZERO, knockOutObservationStep, couponPayment,
                AutoCallPaymentTypeEnum.valueOf(knockInOptionType.name()), knockInStrike, BigDecimal.ZERO, annValRatio);
    }
}
