package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.BasketInstrumentConstituent;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.OptionTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.SpreadsInstrument;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SpreadsOptionDTO {

    @BctField(description = "买卖方向",componentClass = InstrumentOfValuePartyRoleTypeEnum.class)
    public InstrumentOfValuePartyRoleTypeEnum direction;
    public ExerciseTypeEnum exerciseType;
    public ProductTypeEnum productType;
    @BctField(description = "敲出方向",componentClass = UnitEnum.class)
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
    @BctField(description = "",componentClass = OptionTypeEnum.class)
    public OptionTypeEnum optionType;
    @BctField(description = "名义本金",componentClass = BigDecimal.class)
    public BigDecimal notionalAmount;
    @BctField(description = "单位",componentClass = BigDecimal.class)
    public UnitEnum notionalAmountType;
    @BctField(description = "到期日")
    public LocalDate expirationDate;
    @BctField(description = "生效日期")
    public LocalDate effectiveDate;
    @BctField(description = "交易对手")
    public String counterpartyCode;
    @BctField(description = "",componentClass = UnitEnum.class)
    public UnitEnum premiumType;
    @BctField(description = "期权费",componentClass = BigDecimal.class)
    public BigDecimal premium;
    @BctField(description = "期初权利金",componentClass = BigDecimal.class)
    public BigDecimal frontPremium;
    @BctField(description = "最低权利金",componentClass = BigDecimal.class)
    public BigDecimal minimumPremium;
//    @BctField(description = "期限",componentClass = BigDecimal.class)
    public List<Map<String, Object>> constituents;
    @BctField(description = "标的物编号1")
    public String underlyerInstrumentId1;
    @BctField(description = "合约乘数1",componentClass = BigDecimal.class)
    public BigDecimal underlyerMultiplier1;
    public BigDecimal weight1;
    @BctField(description = "期初价格1",componentClass = BigDecimal.class)
    public BigDecimal initialSpot1;
    @BctField(description = "标的物编号2")
    public String underlyerInstrumentId2;
    @BctField(description = "合约乘数2",componentClass = BigDecimal.class)
    public BigDecimal underlyerMultiplier2;
    public BigDecimal weight2;
    @BctField(description = "期初价格2",componentClass = BigDecimal.class)
    public BigDecimal initialSpot2;
    @BctField(description = "年化系数", componentClass = BigDecimal.class)
    public BigDecimal annValRatio;

    public SpreadsOptionDTO() {
    }

    public SpreadsOptionDTO(ExerciseTypeEnum exerciseType, InstrumentOfValuePartyRoleTypeEnum direction,
                            ProductTypeEnum productType, BigDecimal strike, UnitEnum strikeType,
                            BusinessCenterTimeTypeEnum specifiedPrice, LocalDate settlementDate, BigDecimal term,
                            Boolean annualized, BigDecimal daysInYear, BigDecimal participationRate,
                            OptionTypeEnum optionType, BigDecimal notionalAmount, UnitEnum notionalAmountType,
                            LocalDate expirationDate, String counterpartyCode, LocalDate effectiveDate,
                            UnitEnum premiumType, BigDecimal frontPremium, BigDecimal premium,
                            BigDecimal minimumPremium, SpreadsInstrument underlyers, BigDecimal annValRatio) {
        this.exerciseType = exerciseType;
        this.productType = productType;
        this.direction = direction;
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
        this.expirationDate = expirationDate;
        this.counterpartyCode = counterpartyCode;
        this.notionalAmountType = notionalAmountType;
        this.effectiveDate = effectiveDate;
        this.premiumType = premiumType;
        this.frontPremium = frontPremium;
        this.premium = premium;
        this.minimumPremium = minimumPremium;

        List<BasketInstrumentConstituent> constituents = underlyers.constituents();
        //目前默认basket中只有两种instrument
        BasketInstrumentConstituent constituent1 = constituents.get(0);
        BasketInstrumentConstituent constituent2 = constituents.get(1);
        this.underlyerInstrumentId1 = constituent1.instrument().instrumentId();
        this.underlyerMultiplier1 = constituent1.multiplier();
        this.weight1 = constituent1.weight();
        this.initialSpot1 = constituent1.initialSpot();
        this.underlyerInstrumentId2 = constituent2.instrument().instrumentId();
        this.underlyerMultiplier2 = constituent2.multiplier();
        this.weight2 = constituent2.weight();
        this.initialSpot2 = constituent2.initialSpot();
        this.annValRatio = annValRatio;
    }


}
