package tech.tongyu.bct.quant.library.priceable.cash;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.priceable.Cash;
import tech.tongyu.bct.quant.library.priceable.Priceable;

import java.util.List;

public class CashFlows implements Cash, Priceable {
    private final List<CashPayment> cashPayments;

    @JsonCreator
    public CashFlows(List<CashPayment> cashPayments) {
        this.cashPayments = cashPayments;
    }

    public List<CashPayment> getCashPayments() {
        return cashPayments;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.CASH_FLOWS;
    }
}
