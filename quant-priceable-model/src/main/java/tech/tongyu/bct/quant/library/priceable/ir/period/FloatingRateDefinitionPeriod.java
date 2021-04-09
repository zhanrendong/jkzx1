package tech.tongyu.bct.quant.library.priceable.ir.period;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Function;

public class FloatingRateDefinitionPeriod {
    private final LocalDate resetDate;
    private final LocalDate accrualStart;
    private final LocalDate accrualEnd;
    private final double dayCountFraction;
    private final double spread;
    private final Double fixing;

    @JsonCreator
    public FloatingRateDefinitionPeriod(LocalDate resetDate,
                                        LocalDate accrualStart, LocalDate accrualEnd,
                                        double dayCountFraction,
                                        double spread,
                                        Double fixing) {
        this.resetDate = resetDate;
        this.accrualStart = accrualStart;
        this.accrualEnd = accrualEnd;
        this.dayCountFraction = dayCountFraction;
        this.spread = spread;
        this.fixing = fixing;
    }

    public double rate(Function<LocalDate, Double> curve) {
        return (Objects.isNull(fixing) ?
                (curve.apply(accrualStart) / curve.apply(accrualEnd) - 1.0) / dayCountFraction : fixing)
                + spread;
    }

    public LocalDate getResetDate() {
        return resetDate;
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

    public double getSpread() {
        return spread;
    }

    public Double getFixing() {
        return fixing;
    }
}
