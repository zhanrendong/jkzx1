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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Forward<U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        InitialSpotFeature, PremiumFeature, NotionalFeature, SettlementFeature, EffectiveDateFeature, SingleStrikeFeature {
    public Forward() {
    }

    public Forward(U underlyer, BigDecimal underlyerMultiplier, ProductTypeEnum productType,
                   InstrumentAssetClassTypeEnum assetClassType, UnitOfValue<BigDecimal> notionalAmount, LocalDate startDate,
                   LocalDate endDate, AdjustedDate expirationDate, LocalTime expirationTime,
                   BigDecimal initialSpot, LocalDate settlementDate, SettlementTypeEnum settlementType, UnitOfValue<BigDecimal> strike,
                   LocalDate effectiveDate, BusinessCenterTimeTypeEnum specifiedPrice, UnitOfValue<BigDecimal> premium) {
        this.underlyer = underlyer;
        this.productType = productType;
        this.underlyerMultiplier = underlyerMultiplier;
        this.assetClassType = assetClassType;
        this.notionalAmount = notionalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expirationTime = expirationTime;
        this.initialSpot = initialSpot;
        this.settlementDate = settlementDate;
        this.strike = strike;
        this.settlementType = settlementType;
        this.premium = premium;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.specifiedPrice = specifiedPrice;
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
        return String.format("forwards_option_%s_%s",productType(), underlyer().instrumentId());
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
        return new ArrayList<>();
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

    @Override
    public BigDecimal daysInYear() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal term() {
        return BigDecimal.ZERO;
    }

    public boolean annualized = false;
    @Override
    public Boolean isAnnualized() {
        return annualized;
    }

    @Override
    public BigDecimal annValRatio() {
        return null;
    }

    @Override
    public ExerciseTypeEnum exerciseType() {
        return ExerciseTypeEnum.EUROPEAN;
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
