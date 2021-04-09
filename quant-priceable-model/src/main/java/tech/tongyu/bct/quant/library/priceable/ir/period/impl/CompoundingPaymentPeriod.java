package tech.tongyu.bct.quant.library.priceable.ir.period.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import tech.tongyu.bct.quant.library.priceable.ir.period.AccrualPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingPaymentPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.FloatingRateDefinitionPeriod;
import tech.tongyu.bct.quant.library.priceable.ir.period.PeriodTypeEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CompoundingPaymentPeriod implements FloatingPaymentPeriod {
    private final List<AccrualPeriod> accrualPeriods;
    private final List<FloatingRateDefinitionPeriod> definitionPeriods;
    private final LocalDate paymentDate;
    private final double notional;
    private final PeriodTypeEnum periodType;
    // calculated
    private final LocalDate paymentStart;
    private final LocalDate paymentEnd;
    private final double paymentDayCountFraction;

    @JsonCreator
    public CompoundingPaymentPeriod(List<AccrualPeriod> accrualPeriods,
                                    List<FloatingRateDefinitionPeriod> definitionPeriods,
                                    LocalDate paymentDate,
                                    double notional,
                                    PeriodTypeEnum periodType) {
        this.accrualPeriods = accrualPeriods;
        this.definitionPeriods = definitionPeriods;
        this.paymentDate = paymentDate;
        this.notional = notional;
        this.periodType = periodType;
        // calculated
        this.paymentStart = accrualPeriods.get(0).getAccrualStart();
        this.paymentEnd = accrualPeriods.get(accrualPeriods.size() - 1).getAccrualEnd();
        this.paymentDayCountFraction = accrualPeriods.stream().mapToDouble(AccrualPeriod::getDayCountFraction).sum();
    }

    @Override
    public LocalDate paymentDate() {
        return paymentDate;
    }

    @Override
    public double payment(Function<LocalDate, Double> curve) {
        return (IntStream.range(0, accrualPeriods.size())
                .mapToDouble(i -> 1. + accrualPeriods.get(i).getDayCountFraction()
                        * definitionPeriods.get(i).rate(curve))
                .reduce(1., (a, b) -> a * b) - 1.) * notional;
    }

    @Override
    public LocalDate paymentStart() {
        return this.paymentStart;
    }

    @Override
    public LocalDate paymentEnd() {
        return this.paymentEnd;
    }

    @Override
    public double paymentDayCountFraction() {
        return this.paymentDayCountFraction;
    }

    @Override
    public double notional() {
        return this.notional;
    }

    public List<AccrualPeriod> getAccrualPeriods() {
        return accrualPeriods;
    }

    public List<FloatingRateDefinitionPeriod> getDefinitionPeriods() {
        return definitionPeriods;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public double getNotional() {
        return notional;
    }

    public PeriodTypeEnum getPeriodType() {
        return periodType;
    }
}
