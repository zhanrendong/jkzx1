package tech.tongyu.bct.trade.dto.trade.product;

import tech.tongyu.bct.cm.product.iov.CashFlowDirectionEnum;
import tech.tongyu.bct.cm.product.iov.PaymentStateEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashFlowDTO {
    public CashFlowDirectionEnum paymentDirection;
    @BctField(description = "结算日期")
    public LocalDate settlementDate;
    public String currency;
    public BigDecimal paymentAmount;
    public PaymentStateEnum paymentState;


    public CashFlowDTO() {
    }

    public CashFlowDTO(CashFlowDirectionEnum paymentDirection, String currency, BigDecimal paymentAmount,
                       LocalDate settlementDate, PaymentStateEnum paymentState) {
        this.paymentDirection = paymentDirection;
        this.currency = currency;
        this.paymentAmount = paymentAmount;
        this.settlementDate = settlementDate;
        this.paymentState = paymentState;
    }
}
