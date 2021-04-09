package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ForwardDTO {
    @BctField(description = "买卖方向",componentClass = InstrumentOfValuePartyRoleTypeEnum.class)
    public InstrumentOfValuePartyRoleTypeEnum direction;
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
    @BctField(description = "交易对手")
    public String counterpartyCode;
    @BctField(description = "单位",componentClass = UnitEnum.class)
    public UnitEnum premiumType;
    @BctField(description = "期权费",componentClass = BigDecimal.class)
    public BigDecimal premium;
    @BctField(description = "年化否")
    public boolean annualized;
    @BctField(description = "实际期权费", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualPremium;
    @BctField(description = "实际名义本金", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmount;
    @BctField(description = "实际名义本金手数", componentClass = BigDecimal.class)
    public BigDecimal annualizedActualNotionalAmountByLot;

    public ForwardDTO() {
    }

    public ForwardDTO(InstrumentOfValuePartyRoleTypeEnum direction, String underlyerInstrumentId, BigDecimal initialSpot,
                      BigDecimal strike, UnitEnum strikeType, BusinessCenterTimeTypeEnum specifiedPrice,
                      LocalDate settlementDate, BigDecimal notionalAmount, UnitEnum notionalAmountType,
                      BigDecimal underlyerMultiplier, LocalDate expirationDate, String counterpartyCode,
                      LocalDate effectiveDate, UnitEnum premiumType, BigDecimal premium,boolean annualized
            , BigDecimal annualizedActualNotionalAmount, BigDecimal annualizedActualNotionalAmountByLot, BigDecimal annualizedActualPremium) {
        this.direction = direction;
        this.underlyerInstrumentId = underlyerInstrumentId;
        this.initialSpot = initialSpot;
        this.strike = strike;
        this.strikeType = strikeType;
        this.specifiedPrice = specifiedPrice;
        this.settlementDate = settlementDate;
        this.notionalAmount = notionalAmount;
        this.underlyerMultiplier = underlyerMultiplier;
        this.expirationDate = expirationDate;
        this.counterpartyCode = counterpartyCode;
        this.notionalAmountType = notionalAmountType;
        this.effectiveDate = effectiveDate;
        this.premiumType = premiumType;
        this.premium = premium;
        this.annualized = annualized;
        this.annualizedActualNotionalAmount = annualizedActualNotionalAmount;
        this.annualizedActualNotionalAmountByLot = annualizedActualNotionalAmountByLot;
        this.annualizedActualPremium = annualizedActualPremium;
    }
}
