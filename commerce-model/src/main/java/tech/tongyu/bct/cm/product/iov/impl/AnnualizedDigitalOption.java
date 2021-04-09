package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.*;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;
import tech.tongyu.bct.cm.product.iov.feature.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class AnnualizedDigitalOption<U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        InitialSpotFeature, PremiumFeature, SettlementFeature, EffectiveDateFeature, SingleStrikeFeature, RebateFeature,
        ObservationFeature {

    public AnnualizedDigitalOption() {

    }

    public AnnualizedDigitalOption(U underlyer, OptionTypeEnum optionType, BigDecimal underlyerMultiplier, ProductTypeEnum productType,
                                   InstrumentAssetClassTypeEnum assetClassType, BusinessCenterTimeTypeEnum specifiedPrice,
                                   ExerciseTypeEnum exerciseType, BigDecimal initialSpot, BigDecimal participationRate,
                                   UnitOfValue<BigDecimal> notionalAmount, UnitOfValue<BigDecimal> frontPremium,
                                   UnitOfValue<BigDecimal> minimumPremium, UnitOfValue<BigDecimal> premium,
                                   Boolean annualized, BigDecimal daysInYear, BigDecimal term,
                                   SettlementTypeEnum settlementType, LocalDate settlementDate,
                                   LocalDate startDate, LocalDate endDate, LocalDate effectiveDate,
                                   AdjustedDate expirationDate, LocalTime expirationTime,
                                   UnitOfValue<BigDecimal> strike, BarrierObservationTypeEnum observationType,
                                   RebateTypeEnum rebateType, UnitOfValue<BigDecimal> rebate, BigDecimal annValRatio) {
        this.underlyer = underlyer;
        this.optionType = optionType;
        this.underlyerMultiplier = underlyerMultiplier;
        this.productType = productType;
        this.assetClassType = assetClassType;
        this.specifiedPrice = specifiedPrice;
        this.exerciseType = exerciseType;
        this.initialSpot = initialSpot;
        this.participationRate = participationRate;
        this.notionalAmount = notionalAmount;
        this.frontPremium = frontPremium;
        this.minimumPremium = minimumPremium;
        this.premium = premium;
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
        this.strike = strike;
        this.observationType = observationType;
        this.rebateType = rebateType;
        this.rebate = rebate;
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
        return String.format("Digital_%s_option_European_%s",
                optionType(), underlyer().instrumentId());
    }

    public OptionTypeEnum optionType;

    public OptionTypeEnum optionType() {
        return optionType;
    }

    public BigDecimal underlyerMultiplier;

    @Override
    public BigDecimal underlyerMultiplier() {
        return underlyerMultiplier;
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

    public BigDecimal initialSpot;

    @Override
    public BigDecimal initialSpot() {
        return initialSpot;
    }

    public BigDecimal participationRate;

    @Override
    public BigDecimal participationRate() {
        return participationRate;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return null;
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

    public UnitOfValue<BigDecimal> premium;

    @Override
    public UnitOfValue<BigDecimal> premium() {
        return premium;
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


    public UnitOfValue<BigDecimal> strike;

    @Override
    public UnitOfValue<BigDecimal> strike() {
        return strike;
    }

    @Override
    public void strikeChangeLCMEvent(UnitOfValue<BigDecimal> strike) {
        this.strike = strike;
    }

    public BarrierObservationTypeEnum observationType;

    @Override
    public BarrierObservationTypeEnum observationType() {
        return observationType;
    }

    public RebateTypeEnum rebateType;

    @Override
    public RebateTypeEnum rebateType() {
        return rebateType;
    }

    public UnitOfValue<BigDecimal> rebate;

    @Override
    public UnitOfValue<BigDecimal> rebate() {
        return rebate;
    }
}
