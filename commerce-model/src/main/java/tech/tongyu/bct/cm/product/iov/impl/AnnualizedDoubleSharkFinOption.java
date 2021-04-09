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

public class AnnualizedDoubleSharkFinOption<U extends InstrumentOfValue> implements InstrumentOfValue, OptionExerciseFeature,
        InitialSpotFeature, PremiumFeature, SettlementFeature, EffectiveDateFeature, DoubleSharkFinFeature{
    public AnnualizedDoubleSharkFinOption() {

    }

    public AnnualizedDoubleSharkFinOption(U underlyer, BigDecimal underlyerMultiplier, ProductTypeEnum productType,
                                          InstrumentAssetClassTypeEnum assetClassType, BusinessCenterTimeTypeEnum specifiedPrice,
                                          ExerciseTypeEnum exerciseType, BigDecimal initialSpot, UnitOfValue<BigDecimal> notionalAmount,
                                          UnitOfValue<BigDecimal> frontPremium, UnitOfValue<BigDecimal> minimumPremium,
                                          UnitOfValue<BigDecimal> premium, Boolean annualized, BigDecimal daysInYear, BigDecimal term,
                                          SettlementTypeEnum settlementType, LocalDate settlementDate, LocalDate startDate, LocalDate endDate,
                                          LocalDate effectiveDate, AdjustedDate expirationDate, LocalTime expirationTime,
                                          BarrierObservationTypeEnum observationType, BigDecimal lowParticipationRate, BigDecimal highParticipationRate,
                                          UnitOfValue<BigDecimal> lowStrike, UnitOfValue<BigDecimal> highStrike,
                                          UnitOfValue<BigDecimal> lowBarrier, UnitOfValue<BigDecimal> highBarrier,
                                          RebateTypeEnum rebateType, UnitOfValue<BigDecimal> lowRebate, UnitOfValue<BigDecimal> highRebate,
                                          BigDecimal annValRatio) {
        this.underlyer = underlyer;
        this.underlyerMultiplier = underlyerMultiplier;
        this.productType = productType;
        this.assetClassType = assetClassType;
        this.specifiedPrice = specifiedPrice;
        this.exerciseType = exerciseType;
        this.initialSpot = initialSpot;
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
        this.observationType = observationType;
        this.lowParticipationRate = lowParticipationRate;
        this.highParticipationRate = highParticipationRate;
        this.lowStrike = lowStrike;
        this.highStrike = highStrike;
        this.lowBarrier = lowBarrier;
        this.highBarrier = highBarrier;
        this.rebateType = rebateType;
        this.lowRebate = lowRebate;
        this.highRebate = highRebate;
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
        return String.format("DoubleSharkFin_European_%s", underlyer().instrumentId());
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

    public BarrierObservationTypeEnum observationType;

    @Override
    public BarrierObservationTypeEnum observationType() {
        return observationType;
    }

    public BigDecimal lowParticipationRate;

    @Override
    public BigDecimal lowParticipationRate() {
        return lowParticipationRate;
    }

    public BigDecimal highParticipationRate;

    @Override
    public BigDecimal highParticipationRate() {
        return highParticipationRate;
    }

    public KnockDirectionEnum lowDirection = KnockDirectionEnum.DOWN;

    @Override
    public KnockDirectionEnum lowDirection() {
        return lowDirection;
    }

    public KnockDirectionEnum highDirection = KnockDirectionEnum.UP;

    @Override
    public KnockDirectionEnum highDirection() {
        return highDirection;
    }

    public UnitOfValue<BigDecimal> lowStrike;

    @Override
    public UnitOfValue<BigDecimal> lowStrike() {
        return lowStrike;
    }

    public UnitOfValue<BigDecimal> highStrike;

    @Override
    public UnitOfValue<BigDecimal> highStrike() {
        return highStrike;
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

    public RebateTypeEnum rebateType;

    @Override
    public RebateTypeEnum rebateType() {
        return rebateType;
    }

    public UnitOfValue<BigDecimal> lowRebate;

    @Override
    public UnitOfValue<BigDecimal> lowRebate() {
        return lowRebate;
    }

    public UnitOfValue<BigDecimal> highRebate;

    @Override
    public UnitOfValue<BigDecimal> highRebate() {
        return highRebate;
    }


}
