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
import java.util.Arrays;
import java.util.List;

public class AnnualizedSpreadsOption<U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        PremiumFeature, ParticipationRateFeature, SettlementFeature, EffectiveDateFeature, SingleStrikeFeature {
    public AnnualizedSpreadsOption() {

    }

    public AnnualizedSpreadsOption(U underlyer, OptionTypeEnum optionType, ProductTypeEnum productType,
                                   InstrumentAssetClassTypeEnum assetClassType, UnitOfValue<BigDecimal> notionalAmount,
                                   BigDecimal participationRate, LocalDate startDate, LocalDate endDate, BigDecimal daysInYear,
                                   BigDecimal term, Boolean annualized, ExerciseTypeEnum exerciseType,
                                   AdjustedDate expirationDate, LocalTime expirationTime, LocalDate settlementDate,
                                   SettlementTypeEnum settlementType, UnitOfValue<BigDecimal> strike, LocalDate effectiveDate,
                                   BusinessCenterTimeTypeEnum specifiedPrice, UnitOfValue<BigDecimal> frontPremium,
                                   UnitOfValue<BigDecimal> minimumPremium, UnitOfValue<BigDecimal> premium, BigDecimal annValRatio) {
        this.underlyer = underlyer;
        this.productType = productType;
        this.optionType = optionType;
        this.assetClassType = assetClassType;
        this.notionalAmount = notionalAmount;
        this.participationRate = participationRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.daysInYear = daysInYear;
        this.term = term;
        this.annualized = annualized;
        this.exerciseType = exerciseType;
        this.expirationTime = expirationTime;
        this.settlementDate = settlementDate;
        this.strike = strike;
        this.settlementType = settlementType;
        this.frontPremium = frontPremium;
        this.minimumPremium = minimumPremium;
        this.premium = premium;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.specifiedPrice = specifiedPrice;
        this.annValRatio = annValRatio;
    }


    public BigDecimal underlyerMultiplier;

    @Override
    public BigDecimal underlyerMultiplier() {
        return underlyerMultiplier;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public U underlyer;

    @Override
    public U underlyer() {
        return underlyer;
    }

    @Override
    public String instrumentId() {
        return String.format("vanilla_%s_option_$s_%s",
                optionType(), productType(), underlyer().instrumentId());
    }

    public OptionTypeEnum optionType;

    public OptionTypeEnum optionType() {
        return optionType;
    }


    @Override
    public List<InstrumentOfValuePartyRoleTypeEnum> roles() {
        return Arrays.asList(InstrumentOfValuePartyRoleTypeEnum.BUYER, InstrumentOfValuePartyRoleTypeEnum.SELLER);
    }

    public InstrumentAssetClassTypeEnum assetClassType;

    @Override
    public InstrumentAssetClassTypeEnum assetClassType() {
        return assetClassType;
    }

    public ProductTypeEnum productType;

    @Override
    public ProductTypeEnum productType() {
        return productType;
    }

    @Override
    public List<CurrencyUnit> contractCurrencies() {
        return null;
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

    public UnitOfValue<BigDecimal> notionalAmount;

    @Override
    public UnitOfValue<BigDecimal> notionalAmount() {
        return notionalAmount;
    }

    @Override
    public void notionalChangeLCMEvent(UnitOfValue<BigDecimal> notional) {
        this.notionalAmount = notional;
    }

    public BigDecimal participationRate;

    @Override
    public BigDecimal participationRate() {
        return participationRate;
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

    public ExerciseTypeEnum exerciseType;

    @Override
    public ExerciseTypeEnum exerciseType() {
        return exerciseType;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public AdjustedDate expirationDate;

    @Override
    public void rollLCMEvent(LocalDate expirationDate) {
        this.expirationDate = new AbsoluteDate(expirationDate);
        this.settlementDate = expirationDate;
        this.endDate = expirationDate;
    }

    @Override
    public AdjustedDate expirationDate() {
        return expirationDate;
    }


    public BusinessCenterTimeTypeEnum specifiedPrice;

    @Override
    public BusinessCenterTimeTypeEnum specifiedPrice() {
        return specifiedPrice;
    }

    public LocalTime expirationTime;

    @Override
    public LocalTime expirationTime() {
        return expirationTime;
    }

    public BigDecimal initialSpot;

    @Override
    public BigDecimal initialSpot() {
        return initialSpot;
    }

    public LocalDate settlementDate;

    @Override
    public LocalDate settlementDate() {
        return settlementDate;
    }


    public SettlementTypeEnum settlementType;

    @Override
    public SettlementTypeEnum settlementType() {
        return settlementType;
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

    public LocalDate effectiveDate;

    @Override
    public LocalDate effectiveDate() {
        return effectiveDate;
    }
}
