package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.AutoCallPaymentTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.KnockDirectionEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnnualizedAutoCallPhoenixOptionDTO {

    @BctField(description = "买卖方向", componentClass = InstrumentOfValuePartyRoleTypeEnum.class)
    public InstrumentOfValuePartyRoleTypeEnum direction;
    @BctField(description = "结算方式", componentClass = BusinessCenterTimeTypeEnum.class)
    public BusinessCenterTimeTypeEnum specifiedPrice;

    @BctField(description = "标的物编号")
    public String underlyerInstrumentId;
    @BctField(description = "交易对手")
    public String counterpartyCode;
    @BctField(description = "合约乘数", componentClass = BigDecimal.class)
    public BigDecimal underlyerMultiplier;
    @BctField(description = "参与率", componentClass = BigDecimal.class)
    public BigDecimal participationRate;
    @BctField(description = "期初价格", componentClass = BigDecimal.class)
    public BigDecimal initialSpot;
    @BctField(description = "名义本金单位", componentClass = UnitEnum.class)
    public UnitEnum notionalAmountType;
    @BctField(description = "名义本金", componentClass = BigDecimal.class)
    public BigDecimal notionalAmount;

    @BctField(description = "期权费单位", componentClass = UnitEnum.class)
    public UnitEnum premiumType;
    @BctField(description = "期权费", componentClass = BigDecimal.class)
    public BigDecimal premium;
    @BctField(description = "合约期权费", componentClass = BigDecimal.class)
    public BigDecimal frontPremium;
    @BctField(description = "保底收益", componentClass = BigDecimal.class)
    public BigDecimal minimumPremium;

    @BctField(description = "年化否")
    public Boolean annualized;
    @BctField(description = "年度计息天数", componentClass = BigDecimal.class)
    public BigDecimal daysInYear;
    @BctField(description = "期限", componentClass = BigDecimal.class)
    public BigDecimal term;

    @BctField(description = "结算日期")
    public LocalDate settlementDate;
    @BctField(description = "生效日期")
    public LocalDate effectiveDate;
    @BctField(description = "到期日")
    public LocalDate expirationDate;

    @BctField(description = "敲出方向", componentClass = KnockDirectionEnum.class)
    public KnockDirectionEnum knockDirection;
    @BctField(description = "障碍价单位", componentClass = UnitEnum.class)
    public UnitEnum barrierType;
    @BctField(description = "障碍", componentClass = BigDecimal.class)
    public BigDecimal barrier;
    public String knockOutObservationStep;
    @BctField(description = "coupon障碍", componentClass = BigDecimal.class)
    public BigDecimal couponBarrier;
    @BctField(description = "收益/coupon (%)", componentClass = BigDecimal.class)
    public BigDecimal couponPayment;
    @BctField(description = "到期未敲出收益类型", componentClass = AutoCallPaymentTypeEnum.class)
    public AutoCallPaymentTypeEnum autoCallPaymentType;
    @BctField(description = "障碍观察价格")
    public Map<LocalDate, BigDecimal> fixingObservations;
    @BctField(description = "观察支付日")
    public Map<LocalDate, LocalDate> fixingPaymentDates;
    @BctField(description = "是否敲入")
    public Boolean knockedIn;
    @BctField(description = "敲入日期")
    public LocalDate knockInDate;
    @BctField(description = "敲入行权价单位")
    public UnitEnum knockInStrikeType;
    @BctField(description = "敲入行权价")
    public BigDecimal knockInStrike;
    @BctField(description = "敲入障碍价单位")
    public UnitEnum knockInBarrierType;
    @BctField(description = "敲入障碍价")
    public BigDecimal knockInBarrier;
    @BctField(description = "敲入方向")
    public OptionTypeEnum knockInOptionType;
    @BctField(description = "敲入观察频率")
    public String knockInObservationStep;
    @BctField(description = "敲入观察日")
    public List<LocalDate> knockInObservationDates;
    @BctField(description = "实际期权费", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualPremium;
    @BctField(description = "实际名义本金", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmount;
    @BctField(description = "实际名义本金手数", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmountByLot;
    @BctField(description = "年化系数", componentClass = BigDecimal.class)
    public BigDecimal annValRatio;

    public AnnualizedAutoCallPhoenixOptionDTO(InstrumentOfValuePartyRoleTypeEnum direction, BusinessCenterTimeTypeEnum specifiedPrice,
                                              String underlyerInstrumentId, String counterpartyCode, BigDecimal underlyerMultiplier,
                                              BigDecimal participationRate, BigDecimal initialSpot, UnitEnum notionalAmountType,
                                              BigDecimal notionalAmount, UnitEnum premiumType, BigDecimal premium,
                                              BigDecimal frontPremium, BigDecimal minimumPremium, Boolean annualized,
                                              BigDecimal daysInYear, BigDecimal term, LocalDate settlementDate,
                                              LocalDate effectiveDate, LocalDate expirationDate,
                                              KnockDirectionEnum knockDirection, UnitEnum barrierType, BigDecimal barrier,
                                              String knockOutObservationStep, BigDecimal couponBarrier, BigDecimal couponPayment,
                                              AutoCallPaymentTypeEnum autoCallPaymentType,
                                              Map<LocalDate, BigDecimal> fixingObservations, Map<LocalDate, LocalDate> fixingPaymentDates,
                                              Boolean knockedIn,
                                              LocalDate knockInDate, UnitEnum knockInStrikeType, BigDecimal knockInStrike,
                                              UnitEnum knockInBarrierType, BigDecimal knockInBarrier, OptionTypeEnum knockInOptionType,
                                              String knockInObservationStep, List<LocalDate> knockInObservationDates,
                                              BigDecimal annualizedActualNotionalAmount, BigDecimal annualizedActualNotionalAmountByLot,
                                              BigDecimal annualizedActualPremium, BigDecimal annValRatio) {
        this.direction = direction;
        this.specifiedPrice = specifiedPrice;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.counterpartyCode = counterpartyCode;
        this.underlyerMultiplier = underlyerMultiplier;
        this.participationRate = participationRate;
        this.initialSpot = initialSpot;
        this.notionalAmountType = notionalAmountType;
        this.notionalAmount = notionalAmount;
        this.premiumType = premiumType;
        this.premium = premium;
        this.frontPremium = frontPremium;
        this.minimumPremium = minimumPremium;
        this.annualized = annualized;
        this.daysInYear = daysInYear;
        this.term = term;
        this.settlementDate = settlementDate;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.knockDirection = knockDirection;
        this.barrierType = barrierType;
        this.barrier = barrier;
        this.knockOutObservationStep = knockOutObservationStep;
        this.couponBarrier = couponBarrier;
        this.couponPayment = couponPayment;
        this.autoCallPaymentType = autoCallPaymentType;
        this.fixingObservations = fixingObservations;
        this.fixingPaymentDates = fixingPaymentDates;
        this.knockedIn = knockedIn;
        this.knockInDate = knockInDate;
        this.knockInStrikeType = knockInStrikeType;
        this.knockInStrike = knockInStrike;
        this.knockInBarrierType = knockInBarrierType;
        this.knockInBarrier = knockInBarrier;
        this.knockInOptionType = knockInOptionType;
        this.knockInObservationStep = knockInObservationStep;
        this.knockInObservationDates = knockInObservationDates;
        this.annualizedActualNotionalAmount = annualizedActualNotionalAmount;
        this.annualizedActualNotionalAmountByLot = annualizedActualNotionalAmountByLot;
        this.annualizedActualPremium = annualizedActualPremium;
        this.annValRatio = annValRatio;
    }
}
