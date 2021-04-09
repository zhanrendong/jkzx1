package tech.tongyu.bct.quant.library.priceable.ir.period.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.priceable.ir.period.AccrualPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingPaymentPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingRateDefinitionPeriod;

import java.time.LocalDate;
import java.util.function.Function;

public class NonCompoundingPaymentPeriod implements FloatingPaymentPeriod {
    private final AccrualPeriod accrualPeriod;
    private final FloatingRateDefinitionPeriod definitionPeriod;
    private final LocalDate paymentDate;
    private final double notional;

    @JsonCreator
    public NonCompoundingPaymentPeriod(AccrualPeriod accrualPeriod,
                                       FloatingRateDefinitionPeriod definitionPeriod,
                                       LocalDate paymentDate, double notional) {
        this.accrualPeriod = accrualPeriod;
        this.definitionPeriod = definitionPeriod;
        this.paymentDate = paymentDate;
        this.notional = notional;
    }

    @Override
    public LocalDate paymentDate() {
        return paymentDate;
    }

    @Override
    public double payment(Function<LocalDate, Double> curve) {
        return definitionPeriod.rate(curve) * accrualPeriod.getDayCountFraction() * notional;
    }

    @Override
    public LocalDate paymentStart() {
        return accrualPeriod.getAccrualStart();
    }

    @Override
    public LocalDate paymentEnd() {
        return accrualPeriod.getAccrualEnd();
    }

    @Override
    public double paymentDayCountFraction() {
        return accrualPeriod.getDayCountFraction();
    }

    @Override
    public double notional() {
        return this.notional;
    }

    public AccrualPeriod getAccrualPeriod() {
        return accrualPeriod;
    }

    public FloatingRateDefinitionPeriod getDefinitionPeriod() {
        return definitionPeriod;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public double getNotional() {
        return notional;
    }
}
