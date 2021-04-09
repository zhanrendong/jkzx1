package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnnualizedVanillaOptionDTO {
    @BctField(description = "买卖方向",componentClass = InstrumentOfValuePartyRoleTypeEnum.class)
    public InstrumentOfValuePartyRoleTypeEnum direction;
    public ExerciseTypeEnum exerciseType;
    @BctField(description = "标的物编号")
    public String underlyerInstrumentId;
    @BctField(description = "期初价格",componentClass = BigDecimal.class)
    public BigDecimal initialSpot;
    @BctField(description = "单位",componentClass = UnitEnum.class)
    public UnitEnum strikeType;
    @BctField(description = "行权价",componentClass = BigDecimal.class)
    public BigDecimal strike;
    @BctField(description = "结算方式",componentClass = BusinessCenterTimeTypeEnum.class)
    public BusinessCenterTimeTypeEnum specifiedPrice;
    @BctField(description = "结算日期")
    public LocalDate settlementDate;
    @BctField(description = "期限",componentClass = BigDecimal.class)
    public BigDecimal term;
    @BctField(description = "年化否")
    public Boolean annualized;
    @BctField(description = "年度计息天数",componentClass = BigDecimal.class)
    public BigDecimal daysInYear;
    @BctField(description = "参与率",componentClass = BigDecimal.class)
    public BigDecimal participationRate;
    @BctField(description = "看跌看涨",componentClass = OptionTypeEnum.class)
    public OptionTypeEnum optionType;
    @BctField(description = "名义本金",componentClass = BigDecimal.class)

    public BigDecimal notionalAmount;
    @BctField(description = "单位",componentClass = UnitEnum.class)
    public UnitEnum notionalAmountType;
    @BctField(description = "合约乘数",componentClass = BigDecimal.class)
    public BigDecimal underlyerMultiplier;
    @BctField(description = "到期日")
    public LocalDate expirationDate;
    @BctField(description = "生效日期")
    public LocalDate effectiveDate;
    @BctField(description = "竞争对手",componentClass = BigDecimal.class)
    public String counterpartyCode;
    @BctField(description = "单位",componentClass = UnitEnum.class)
    public UnitEnum premiumType;
    @BctField(description = "期权费",componentClass = BigDecimal.class)
    public BigDecimal premium;
    @BctField(description = "期初权利金",componentClass = BigDecimal.class)
    public BigDecimal frontPremium;
    @BctField(description = "保底收益",componentClass = BigDecimal.class)
    public BigDecimal minimumPremium;
    @BctField(description = "实际期权费", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualPremium;
    @BctField(description = "实际名义本金", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmount;
    @BctField(description = "实际名义本金手数", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmountByLot;
    @BctField(description = "年化系数", componentClass = BigDecimal.class)
    public BigDecimal annValRatio;

    public AnnualizedVanillaOptionDTO() {
    }

    public AnnualizedVanillaOptionDTO(ExerciseTypeEnum exerciseType, InstrumentOfValuePartyRoleTypeEnum direction, String underlyerInstrumentId,
                                      BigDecimal initialSpot, BigDecimal strike, UnitEnum strikeType, BusinessCenterTimeTypeEnum specifiedPrice,
                                      LocalDate settlementDate, BigDecimal term,
                                      Boolean annualized, BigDecimal daysInYear, BigDecimal participationRate,
                                      OptionTypeEnum optionType, BigDecimal notionalAmount, UnitEnum notionalAmountType, BigDecimal underlyerMultiplier,
                                      LocalDate expirationDate, String counterpartyCode, LocalDate effectiveDate, UnitEnum premiumType, BigDecimal frontPremium,
                                      BigDecimal premium, BigDecimal minimumPremium
            , BigDecimal annualizedActualNotionalAmount, BigDecimal annualizedActualNotionalAmountByLot, BigDecimal annualizedActualPremium, BigDecimal annValRatio) {
        this.exerciseType = exerciseType;
        this.direction = direction;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.initialSpot = initialSpot;
        this.strike = strike;
        this.strikeType = strikeType;
        this.specifiedPrice = specifiedPrice;
        this.settlementDate = settlementDate;
        this.term = term;
        this.annualized = annualized;
        this.daysInYear = daysInYear;
        this.participationRate = participationRate;
        this.optionType = optionType;
        this.notionalAmount = notionalAmount;
        this.underlyerMultiplier = underlyerMultiplier;
        this.expirationDate = expirationDate;
        this.counterpartyCode = counterpartyCode;
        this.notionalAmountType = notionalAmountType;
        this.effectiveDate = effectiveDate;
        this.premiumType = premiumType;
        this.frontPremium = frontPremium;
        this.premium = premium;
        this.minimumPremium = minimumPremium;
        this.annualizedActualNotionalAmount = annualizedActualNotionalAmount;
        this.annualizedActualNotionalAmountByLot = annualizedActualNotionalAmountByLot;
        this.annualizedActualPremium = annualizedActualPremium;
        this.annValRatio = annValRatio;
    }
}
