package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AnnualizedAutoCallOption <U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        InitialSpotFeature, PremiumFeature, SettlementFeature, EffectiveDateFeature, BarrierStepFeature, CouponFeature,
        AutoCallExerciseFeature{

    public AnnualizedAutoCallOption() {
    }


    public AnnualizedAutoCallOption(U underlyer, ProductTypeEnum productType, InstrumentAssetClassTypeEnum assetClassType,
                                    BusinessCenterTimeTypeEnum specifiedPrice, ExerciseTypeEnum exerciseType,
                                    BigDecimal underlyerMultiplier, BigDecimal participationRate, BigDecimal initialSpot,
                                    UnitOfValue<BigDecimal> notionalAmount, UnitOfValue<BigDecimal> premium,
                                    UnitOfValue<BigDecimal> frontPremium, UnitOfValue<BigDecimal> minimumPremium,
                                    Boolean annualized, BigDecimal daysInYear, BigDecimal term,
                                    SettlementTypeEnum settlementType, LocalDate settlementDate,
                                    LocalDate startDate, LocalDate endDate, LocalDate effectiveDate,
                                    AdjustedDate expirationDate, LocalTime expirationTime,
                                    Map<LocalDate, BigDecimal> fixingObservations, Map<LocalDate, LocalDate> fixingPaymentDates,
                                    Map<LocalDate, BigDecimal> observationBarriers, KnockDirectionEnum knockDirection,
                                    UnitOfValue<BigDecimal> barrier, BigDecimal step, String knockOutObservationStep,
                                    BigDecimal couponPayment, AutoCallPaymentTypeEnum autoCallPaymentType,
                                    UnitOfValue<BigDecimal> autoCallStrike, BigDecimal fixedPayment, BigDecimal annValRatio) {
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
        this.fixingObservations = fixingObservations;
        this.fixingPaymentDates = fixingPaymentDates;
        this.observationBarriers = observationBarriers;
        this.knockDirection = knockDirection;
        this.barrier = barrier;
        this.step = step;
        this.knockOutObservationStep = knockOutObservationStep;
        this.couponPayment = couponPayment;
        this.autoCallPaymentType = autoCallPaymentType;
        this.autoCallStrike = autoCallStrike;
        this.fixedPayment = fixedPayment;
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
        return String.format("AutoCall_option_European_%s",
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

    public List<LocalDate> observationDates() {
        List<LocalDate> dates = new ArrayList<>(fixingObservations.size());
        dates.addAll(fixingObservations.keySet());
        Collections.sort(dates);
        return dates;
    }

    public Map<LocalDate, BigDecimal> observedFixings() {
        HashMap<LocalDate, BigDecimal> fixings = new HashMap<>();
        fixingObservations.forEach((d, f) -> {
            if (!Objects.isNull(f)) {
                fixings.put(d, f);
            }
        });
        return fixings;
    }

    @Override
    public void observationLCMEvent(LocalDate observationDate, BigDecimal observationPrice) {
        fixingObservations.put(observationDate, observationPrice);
    }

    public KnockDirectionEnum knockDirection;

    @Override
    public KnockDirectionEnum knockDirection() {
        return knockDirection;
    }

    @Override
    public BarrierObservationTypeEnum observationType() {
        return BarrierObservationTypeEnum.DISCRETE;
    }

    public UnitOfValue<BigDecimal> barrier;

    @Override
    public UnitOfValue<BigDecimal> barrier() {
        return barrier;
    }

    public Map<LocalDate, BigDecimal> observationBarriers;

    public Map<LocalDate, BigDecimal> observationBarriers(){
        BigDecimal initialSpot = initialSpot();
        BigDecimal barrierOrigin = barrierValue();
        BigDecimal barrierStep = step().multiply(initialSpot);
        AtomicInteger countAto = new AtomicInteger(0);
        return observationBarriers.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(item -> {
                    int count = countAto.getAndIncrement();
                    BigDecimal barrierValue = item.getValue();
                    if (Objects.isNull(barrierValue)){
                        barrierValue = barrierOrigin.add(barrierStep.multiply(BigDecimal.valueOf(count)));
                    }
                    item.setValue(barrierValue);
                    return item;
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public BigDecimal step;

    @Override
    public BigDecimal step() {
        return step;
    }

    public String knockOutObservationStep;

    public String knockOutObservationStep(){
        return knockOutObservationStep;
    }

    public List<BigDecimal> barrierValues() {
        return observationBarriers().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public UnitOfValue<BigDecimal> couponBarrier() {
        return null;
    }

    public BigDecimal couponPayment;

    @Override
    public BigDecimal couponPayment() {
        return couponPayment;
    }

    public List<BigDecimal> couponPaymentAmounts() {
        List<LocalDate> observationDates = observationDates();
        BigDecimal annualCoupon = couponPayment.multiply(notionalAmountFaceValue()).multiply(participationRate());
        BigDecimal aYear = BigDecimal.valueOf(365);
        return observationDates.stream()
                .map(d -> annualCoupon.multiply(
                        BigDecimal.valueOf(startDate.until(d, ChronoUnit.DAYS))).divide(aYear, 10, BigDecimal.ROUND_DOWN))
                .collect(Collectors.toList());
    }

    public AutoCallPaymentTypeEnum autoCallPaymentType;

    @Override
    public AutoCallPaymentTypeEnum autoCallPaymentType() {
        return autoCallPaymentType;
    }

    public UnitOfValue<BigDecimal> autoCallStrike;

    @Override
    public UnitOfValue<BigDecimal> autoCallStrike() {
        return autoCallStrike;
    }

    public BigDecimal autoCallStrikeAmount() {
        if (autoCallStrike.unit() instanceof Percent) {
            return autoCallStrike.value().multiply(initialSpot);
        }
        return autoCallStrike.value();
    }

    public BigDecimal fixedPayment;

    @Override
    public BigDecimal fixedPayment() {
        return fixedPayment;
    }

    public BigDecimal fixedPaymentAmount() {
        return fixedPayment.multiply(notionalWithParticipation());
    }
}
