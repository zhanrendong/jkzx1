package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnnualizedConcavaConvexOptionDTO {

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
    @BctField(description = "是否凹式")
    public Boolean concavaed;
    @BctField(description = "障碍价单位", componentClass = UnitEnum.class)
    public UnitEnum barrierType;
    @BctField(description = "低障碍价", componentClass = BigDecimal.class)
    public BigDecimal lowBarrier;
    @BctField(description = "高障碍价", componentClass = BigDecimal.class)
    public BigDecimal highBarrier;
    @BctField(description = "收益单位", componentClass = UnitEnum.class)
    public UnitEnum paymentType;
    @BctField(description = "收益", componentClass = BigDecimal.class)
    public BigDecimal payment;

    @BctField(description = "实际期权费", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualPremium;
    @BctField(description = "实际名义本金", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmount;
    @BctField(description = "实际名义本金手数", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmountByLot;
    @BctField(description = "年化系数", componentClass = BigDecimal.class)
    public BigDecimal annValRatio;

    public AnnualizedConcavaConvexOptionDTO(InstrumentOfValuePartyRoleTypeEnum direction, BusinessCenterTimeTypeEnum specifiedPrice,
                                            String underlyerInstrumentId, String counterpartyCode, BigDecimal underlyerMultiplier,
                                            BigDecimal participationRate, BigDecimal initialSpot,
                                            UnitEnum notionalAmountType, BigDecimal notionalAmount,
                                            UnitEnum premiumType, BigDecimal premium, BigDecimal frontPremium, BigDecimal minimumPremium,
                                            Boolean annualized, BigDecimal daysInYear, BigDecimal term, LocalDate settlementDate,
                                            LocalDate effectiveDate, LocalDate expirationDate, Boolean concavaed,
                                            UnitEnum barrierType, BigDecimal lowBarrier, BigDecimal highBarrier,
                                            UnitEnum paymentType, BigDecimal payment, BigDecimal annualizedActualNotionalAmount,
                                            BigDecimal annualizedActualNotionalAmountByLot, BigDecimal annualizedActualPremium,
                                            BigDecimal annValRatio) {
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
        this.concavaed = concavaed;
        this.barrierType = barrierType;
        this.lowBarrier = lowBarrier;
        this.highBarrier = highBarrier;
        this.paymentType = paymentType;
        this.payment = payment;
        this.annualizedActualNotionalAmount = annualizedActualNotionalAmount;
        this.annualizedActualNotionalAmountByLot = annualizedActualNotionalAmountByLot;
        this.annualizedActualPremium = annualizedActualPremium;
        this.annValRatio = annValRatio;
    }
}
