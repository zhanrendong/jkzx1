package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnnualizedTripleDigitalOptionDTO {

    @BctField(description = "买卖方向",componentClass = InstrumentOfValuePartyRoleTypeEnum.class)
    public InstrumentOfValuePartyRoleTypeEnum direction;
    @BctField(description = "结算方式",componentClass = BusinessCenterTimeTypeEnum.class)
    public BusinessCenterTimeTypeEnum specifiedPrice;
    @BctField(description = "看跌看涨",componentClass = OptionTypeEnum.class)
    public OptionTypeEnum optionType;
    @BctField(description = "标的物编号")
    public String underlyerInstrumentId;
    @BctField(description = "竞争对手")
    public String counterpartyCode;
    @BctField(description = "合约乘数",componentClass = BigDecimal.class)
    public BigDecimal underlyerMultiplier;
    @BctField(description = "参与率",componentClass = BigDecimal.class)
    public BigDecimal participationRate;
    @BctField(description = "期初价格",componentClass = BigDecimal.class)
    public BigDecimal initialSpot;
    @BctField(description = "单位",componentClass = UnitEnum.class)
    public UnitEnum notionalAmountType;
    @BctField(description = "名义本金",componentClass = BigDecimal.class)
    public BigDecimal notionalAmount;
    @BctField(description = "单位",componentClass = UnitEnum.class)
    public UnitEnum premiumType;
    @BctField(description = "期权费",componentClass = BigDecimal.class)
    public BigDecimal premium;
    @BctField(description = "前端收益",componentClass = BigDecimal.class)
    public BigDecimal frontPremium;
    @BctField(description = "保底收益",componentClass = BigDecimal.class)
    public BigDecimal minimumPremium;
    @BctField(description = "年化否")
    public Boolean annualized;
    @BctField(description = "年度计息天数",componentClass = BigDecimal.class)
    public BigDecimal daysInYear;
    @BctField(description = "期限",componentClass = BigDecimal.class)
    public BigDecimal term;
    @BctField(description = "结算日期")
    public LocalDate settlementDate;
    @BctField(description = "生效日期")
    public LocalDate effectiveDate;
    @BctField(description = "到期日")
    public LocalDate expirationDate;
    @BctField(description = "行权价单位",componentClass = UnitEnum.class)
    public UnitEnum strikeType;
    @BctField(description = "行权价1",componentClass = BigDecimal.class)
    public BigDecimal strike1;
    @BctField(description = "行权价2",componentClass = BigDecimal.class)
    public BigDecimal strike2;
    @BctField(description = "行权价3",componentClass = BigDecimal.class)
    public BigDecimal strike3;
    @BctField(description = "收益单位",componentClass = UnitEnum.class)
    public UnitEnum paymentType;
    @BctField(description = "收益1",componentClass = BigDecimal.class)
    public BigDecimal payment1;
    @BctField(description = "收益2",componentClass = BigDecimal.class)
    public BigDecimal payment2;
    @BctField(description = "收益3",componentClass = BigDecimal.class)
    public BigDecimal payment3;
    @BctField(description = "实际期权费", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualPremium;
    @BctField(description = "实际名义本金", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmount;
    @BctField(description = "实际名义本金手数", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmountByLot;

    @BctField(description = "年化系数", componentClass = BigDecimal.class)
    public BigDecimal annValRatio;

    public AnnualizedTripleDigitalOptionDTO(InstrumentOfValuePartyRoleTypeEnum direction, BusinessCenterTimeTypeEnum specifiedPrice,
                                            OptionTypeEnum optionType, String underlyerInstrumentId, String counterpartyCode,
                                            BigDecimal underlyerMultiplier, BigDecimal participationRate, BigDecimal initialSpot,
                                            UnitEnum notionalAmountType, BigDecimal notionalAmount, UnitEnum premiumType,
                                            BigDecimal premium, BigDecimal frontPremium, BigDecimal minimumPremium,
                                            Boolean annualized, BigDecimal daysInYear, BigDecimal term, LocalDate settlementDate,
                                            LocalDate effectiveDate, LocalDate expirationDate, UnitEnum strikeType,
                                            BigDecimal strike1, BigDecimal strike2, BigDecimal strike3, UnitEnum paymentType,
                                            BigDecimal payment1, BigDecimal payment2, BigDecimal payment3
            , BigDecimal annualizedActualNotionalAmount, BigDecimal annualizedActualNotionalAmountByLot, BigDecimal annualizedActualPremium,
                                            BigDecimal annValRatio) {
        this.direction = direction;
        this.specifiedPrice = specifiedPrice;
        this.optionType = optionType;
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
        this.strikeType = strikeType;
        this.strike1 = strike1;
        this.strike2 = strike2;
        this.strike3 = strike3;
        this.paymentType = paymentType;
        this.payment1 = payment1;
        this.payment2 = payment2;
        this.payment3 = payment3;
        this.annualizedActualNotionalAmount = annualizedActualNotionalAmount;
        this.annualizedActualNotionalAmountByLot = annualizedActualNotionalAmountByLot;
        this.annualizedActualPremium = annualizedActualPremium;
        this.annValRatio = annValRatio;
    }
}
