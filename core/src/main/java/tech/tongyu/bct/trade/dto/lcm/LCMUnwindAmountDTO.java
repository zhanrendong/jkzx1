package tech.tongyu.bct.trade.dto.lcm;

import tech.tongyu.bct.cm.reference.impl.UnitEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;

public class LCMUnwindAmountDTO {
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    private String tradeId;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    private String positionId;
    @BctField(name = "valueUnit", description = "单位", type = "UnitEnum")
    private UnitEnum valueUnit;
    @BctField(name = "remainValue", description = "剩余数量", type = "BigDecimal")
    private BigDecimal remainValue;
    @BctField(name = "initialValue", description = "初始数量", type = "BigDecimal")
    private BigDecimal initialValue;
    @BctField(name = "historyValue", description = "历史记录", type = "BigDecimal")
    private BigDecimal historyValue;
    @BctField(name = "underlyerMultiplier", description = "合约乘数", type = "BigDecimal")
    private BigDecimal underlyerMultiplier;
    @BctField(name = "initialSpot", description = "期初价格", type = "BigDecimal")
    private BigDecimal initialSpot;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public UnitEnum getValueUnit() {
        return valueUnit;
    }

    public void setValueUnit(UnitEnum valueUnit) {
        this.valueUnit = valueUnit;
    }

    public BigDecimal getRemainValue() {
        return remainValue;
    }

    public void setRemainValue(BigDecimal remainValue) {
        this.remainValue = remainValue;
    }

    public BigDecimal getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(BigDecimal initialValue) {
        this.initialValue = initialValue;
    }

    public BigDecimal getHistoryValue() {
        return historyValue;
    }

    public void setHistoryValue(BigDecimal historyValue) {
        this.historyValue = historyValue;
    }

    public BigDecimal getUnderlyerMultiplier() {
        return underlyerMultiplier;
    }

    public void setUnderlyerMultiplier(BigDecimal underlyerMultiplier) {
        this.underlyerMultiplier = underlyerMultiplier;
    }

    public BigDecimal getInitialSpot() {
        return initialSpot;
    }

    public void setInitialSpot(BigDecimal initialSpot) {
        this.initialSpot = initialSpot;
    }
}
