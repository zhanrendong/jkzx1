package tech.tongyu.bct.quant.library.priceable.cash;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.tongyu.bct.quant.library.priceable.Cash;
import tech.tongyu.bct.quant.library.priceable.Priceable;

import java.time.LocalDate;

public class CashPayment implements Cash, Priceable {
    private final double amount;
    private final LocalDate paymentDate;

    @JsonCreator
    public CashPayment(LocalDate paymentDate, double amount) {
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    @JsonIgnore
    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.CASH_PAYMENT;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
}
