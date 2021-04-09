package tech.tongyu.bct.cm.product.iov.component;

import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.reference.impl.CurrencyUnit;
import tech.tongyu.bct.cm.reference.impl.UnitOfValue;

import java.math.BigDecimal;

public class Payment {

    public BigDecimal paymentAmount;

    public CurrencyUnit currency;

    public AbsoluteDate paymentDate;

    public Payment(BigDecimal payment, CurrencyUnit currency, AbsoluteDate paymentDate) {
        this.paymentAmount = payment;
        this.currency = currency;
        this.paymentDate = paymentDate;
    }

    public Payment(Double paymentAmount, CurrencyUnit currency, AbsoluteDate paymentDate) {
        this.paymentAmount = BigDecimal.valueOf(paymentAmount);
        this.currency = currency;
        this.paymentDate = paymentDate;
    }

    public BigDecimal paymentAmount() {
        return paymentAmount;
    }

    public CurrencyUnit currency() {
        return currency;
    }

    public AbsoluteDate paymentDate() {
        return paymentDate;
    }
}
