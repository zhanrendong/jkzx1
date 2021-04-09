package tech.tongyu.bct.trade.dto.event;

import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;

public class CashFlowCollectionDTO {
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    public String tradeId;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    public String positionId;
    @BctField(name = "open", description = "开仓", type = "BigDecimal")
    public BigDecimal open;
    @BctField(name = "other", description = "其他", type = "BigDecimal")
    public BigDecimal other;
    @BctField(name = "settle", description = "节算", type = "BigDecimal")
    public BigDecimal settle;
    @BctField(name = "unwind", description = "平仓", type = "BigDecimal")
    public BigDecimal unwind;
    @BctField(name = "unwindNumber", description = "平仓数量", type = "BigDecimal")
    public BigDecimal unwindNumber;
    @BctField(name = "initialNumber", description = "初期数量", type = "BigDecimal")
    public BigDecimal initialNumber;
    @BctField(name = "errorMessage", description = "错误信息", type = "String")
    public String errorMessage;

    public CashFlowCollectionDTO(String tradeId, String positionId) {
        this.tradeId = tradeId;
        this.positionId = positionId;
        this.open = BigDecimal.ZERO;
        this.other = BigDecimal.ZERO;
        this.settle = BigDecimal.ZERO;
        this.unwind = BigDecimal.ZERO;
        this.unwindNumber = BigDecimal.ZERO;
        this.initialNumber = BigDecimal.ZERO;
    }

    public CashFlowCollectionDTO() {

    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public void setOther(BigDecimal other) {
        this.other = other;
    }

    public void setSettle(BigDecimal settle) {
        this.settle = settle;
    }

    public void setUnwind(BigDecimal unwind) {
        this.unwind = unwind;
    }

    public void setUnwindNumber(BigDecimal unwindNumber) {
        this.unwindNumber = unwindNumber;
    }

    public void setInitialNumber(BigDecimal initialNumber) {
        this.initialNumber = initialNumber;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
