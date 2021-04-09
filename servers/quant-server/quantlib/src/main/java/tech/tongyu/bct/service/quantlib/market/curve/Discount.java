package tech.tongyu.bct.service.quantlib.market.curve;

import tech.tongyu.bct.service.quantlib.financial.dateservice.Basis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface Discount {
    /**
     * Get the discounting factor on a given date.
     * This is the fundamental functionality of a discounting curve
     * @param t
     * @return
     */
    double df(LocalDateTime t);

    /***
     * Roll a curve to a newer valuation date
     * @param newVal The new valuation date
     * @return A new discounting curve (the original curve is intact)
     */
    Discount roll(LocalDateTime newVal);

    /**
     * Calculates the forward rate between given start and end dates.
     * This is a simple application of df() function.
     * @param start Forward rate start date
     * @param basis Forward rate end date
     * @return The forward rate between start and end dates
     */
    default double fwd(LocalDate start, LocalDate end, Basis basis) {
        double dcf = basis.daycountFraction(start, end);
        double dfStart = df(LocalDateTime.of(start, LocalTime.MIDNIGHT));
        double dfEnd = df(LocalDateTime.of(end, LocalTime.MIDNIGHT));
        return (dfStart/dfEnd - 1.0) / dcf;
    };
    /**
     * Get curve valuation date. This date is usually considered as the curve start date
     * @return Curve valuation date
     */
    LocalDateTime getVal();
}
