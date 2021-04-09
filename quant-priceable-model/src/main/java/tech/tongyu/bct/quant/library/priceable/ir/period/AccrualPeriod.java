package tech.tongyu.bct.quant.library.priceable.ir.period;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDate;

public class AccrualPeriod {

    private final LocalDate accrualStart;
    private final LocalDate accrualEnd;
    private final double dayCountFraction;
    private final PeriodTypeEnum periodType;

    @JsonCreator
    public AccrualPeriod(LocalDate accrualStart, LocalDate accrualEnd, double basis, PeriodTypeEnum periodType) {
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.dayCountFraction = basis;
        this.periodType = periodType;
    }

    public LocalDate getAccrualStart() {
        return accrualStart;
    }

    public LocalDate getAccrualEnd() {
        return accrualEnd;
    }

    public double getDayCountFraction() {
        return dayCountFraction;
    }

    public PeriodTypeEnum getPeriodType() {
        return periodType;
    }
}
