package tech.tongyu.bct.quant.library.market.curve;

import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.financial.date.DayCountBasis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;

public interface DiscountingCurve extends Function<LocalDate, Double>, QuantlibSerializableObject {
    /**
     * Get the discounting factor on a given date.
     * This is the fundamental functionality of a discounting curve
     * @param t
     * @return
     */
    double df(LocalDateTime t);

    double df(LocalDate t);

    /***
     * Get short rate from the curve. Short rate is defined as: -\partial ln(df) \partial dt
     * Implementations can vary depending on how the curve data structure. The most generic
     * methodology is fininite difference. For PWLF curve, short rate is stored in segments and
     * can be returned directly.
     *
     * WARNING: It is tempting to call this function when computing local vol etc. for PDE or MC, but really
     * one should adjust discretized drift terms. This function is for convenience only.
     *
     * WARNING: the return is short rate in *days*. To convert to annualized rate, multiply days in year.
     * @param t the time on which short rate is required
     * @return Short rate at time t
     */
    double shortRate(LocalDateTime t);


    /***
     * Roll a curve to a newer valuation date
     * @param newVal The new valuation date
     * @return A new discounting curve (the original curve is intact)
     */
    DiscountingCurve roll(LocalDateTime newVal);

    /**
     * Bump the whole curve by a fixed amount
     * WARNING: This method assumes continuous compounding which is NOT the general case
     * @param amount The amount to bump, i.e. exp(-(r+amount)*t)
     * @return
     */
    DiscountingCurve bump(double amount);

    /**
     * Bump the curve points by a percentage
     * WARNING: This method assumes continuous compounding which is NOT the general case
     * @param percent
     * @return Bumped curve
     */
    DiscountingCurve bumpPercent(double percent);

    /**
     * Calculates the forward rate between given start and end dates.
     * This is a simple application of df() function.
     * @param start Forward rate start date
     * @param basis Forward rate end date
     * @return The forward rate between start and end dates
     */
    default double fwd(LocalDate start, LocalDate end, DayCountBasis basis) {
        double dcf = basis.daycountFraction(start, end);
        double dfStart = df(LocalDateTime.of(start, LocalTime.MIDNIGHT));
        double dfEnd = df(LocalDateTime.of(end, LocalTime.MIDNIGHT));
        return (dfStart/dfEnd - 1.0) / dcf;
    }
    /**
     * Get curve valuation date. This date is usually considered as the curve start date
     * @return Curve valuation date
     */
    LocalDate getValuationDate();

    LocalDateTime getValuationDateTime();
}
