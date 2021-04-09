package tech.tongyu.bct.quant.library.priceable.ir.leg;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.ir.period.FixedPaymentPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class FixedLeg implements Leg {
    private final List<FixedPaymentPeriod> periods;

    @JsonCreator
    public FixedLeg(List<FixedPaymentPeriod> periods) {
        this.periods = periods;
    }

    public List<CashPayment> cashFlows(LocalDate asOfDate) {
        return this.periods.stream()
                .filter(p -> p.getPaymentDate().isAfter(asOfDate))
                .map(FixedPaymentPeriod::cashFlow)
                .collect(Collectors.toList());
    }

    @Override
    public List<CashPayment> dv01CashFlows(LocalDate asOf) {
        return this.periods.stream()
                .filter(p -> p.paymentDate().isAfter(asOf))
                .map(p -> new CashPayment(p.paymentDate(), p.paymentDayCountFraction() * p.getNotional()))
                .collect(Collectors.toList());
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.FIXED_LEG;
    }

    public List<FixedPaymentPeriod> getPeriods() {
        return periods;
    }
}
