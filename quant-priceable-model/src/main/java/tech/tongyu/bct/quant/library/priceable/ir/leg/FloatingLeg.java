package tech.tongyu.bct.quant.library.priceable.ir.leg;

import tech.tongyu.bct.quant.library.priceable.cash.CashPayment;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingPaymentPeriod;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FloatingLeg implements Leg {
    private final List<FloatingPaymentPeriod> periods;

    public FloatingLeg(List<FloatingPaymentPeriod> periods) {
        this.periods = periods;
    }

    public List<CashPayment> cashFlows(LocalDate asOf, Function<LocalDate, Double> curve) {
        return periods.stream()
                .filter(p -> p.paymentDate().isAfter(asOf))
                .map(p -> p.cashFlow(curve))
                .collect(Collectors.toList());
    }

    @Override
    public List<CashPayment> dv01CashFlows(LocalDate asOf) {
        return this.periods.stream()
                .filter(p -> p.paymentDate().isAfter(asOf))
                .map(p -> new CashPayment(p.paymentDate(), p.paymentDayCountFraction() * p.notional()))
                .collect(Collectors.toList());
    }

    public List<FloatingPaymentPeriod> getPeriods() {
        return periods;
    }

    @Override
    public PriceableTypeEnum getPriceableTypeEnum() {
        return PriceableTypeEnum.FLOATING_LEG;
    }
}
