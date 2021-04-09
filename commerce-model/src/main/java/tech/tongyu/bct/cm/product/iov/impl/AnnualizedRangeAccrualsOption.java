package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.*;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class AnnualizedRangeAccrualsOption <U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        InitialSpotFeature, PremiumFeature, SettlementFeature, EffectiveDateFeature, DoubleBarrierFeature, PaymentFeature,
        ObservationStepFeature, FixingWeightFeature{

    public AnnualizedRangeAccrualsOption() {
    }

    public AnnualizedRangeAccrualsOption(U underlyer, ProductTypeEnum productType, InstrumentAssetClassTypeEnum assetClassType,
                                         BusinessCenterTimeTypeEnum specifiedPrice, ExerciseTypeEnum exerciseType,
                                         BigDecimal underlyerMultiplier, BigDecimal participationRate, BigDecimal initialSpot,
                                         UnitOfValue<BigDecimal> notionalAmount, UnitOfValue<BigDecimal> premium,
                                         UnitOfValue<BigDecimal> frontPremium, UnitOfValue<BigDecimal> minimumPremium,
                                         Boolean annualized, BigDecimal daysInYear, BigDecimal term,
                                         SettlementTypeEnum settlementType, LocalDate settlementDate,
                                         LocalDate startDate, LocalDate endDate, LocalDate effectiveDate,
                                         AdjustedDate expirationDate, LocalTime expirationTime,
                                         UnitOfValue<BigDecimal> lowBarrier, UnitOfValue<BigDecimal> highBarrier,
                                         UnitOfValue<BigDecimal> payment, Map<LocalDate, BigDecimal> fixingObservations,
                                         String observationStep, BigDecimal annValRatio) {
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
        this.lowBarrier = lowBarrier;
        this.highBarrier = highBarrier;
        this.payment = payment;
        this.fixingObservations = fixingObservations;
        this.observationStep = observationStep;
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
        return String.format("RangeAccruals_European_%s",
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

    public UnitOfValue<BigDecimal> lowBarrier;

    @Override
    public UnitOfValue<BigDecimal> lowBarrier() {
        return lowBarrier;
    }

    public UnitOfValue<BigDecimal> highBarrier;

    @Override
    public UnitOfValue<BigDecimal> highBarrier() {
        return highBarrier;
    }

    public UnitOfValue<BigDecimal> payment;

    @Override
    public UnitOfValue<BigDecimal> payment() {
        return payment;
    }

    public String observationStep;

    @Override
    public String observationStep() {
        return observationStep;
    }

    public Map<LocalDate, BigDecimal> fixingObservations;

    @Override
    public Map<LocalDate, BigDecimal> fixingObservations() {
        return fixingObservations;
    }

    @Override
    public Map<LocalDate, LocalDate> fixingPaymentDates() {
        return new HashMap<>();
    }

    @Override
    public void observationLCMEvent(LocalDate observationDate, BigDecimal observationPrice) {
        fixingObservations.put(observationDate, observationPrice);
    }

    @Override
    public Map<LocalDate, BigDecimal> fixingWeights() {
        return fixingObservations().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> BigDecimal.ONE));
    }
}
