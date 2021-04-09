package tech.tongyu.bct.quant.library.financial.date;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Day count basis determines how interest accrues over time. It determines the number of days between two
 * payment dates. There are several day count basis conventions and below have been implemented:
 * <ul>
 *     <li>{@link #ACT360}</li>
 *     <li>{@link #ACT365}</li>
 *     <li>{@link #ACT36525}</li>
 *     <li>{@link #B30360}</li>
 * </ul>
 * @see <a href="https://en.wikipedia.org/wiki/Day_count_convention">Wiki: Date Count Convention</a>
 */
public enum DayCountBasis {
    /**
     * Actual number of days over one year (360 days).
     */
    ACT360 {
        @Override
        public double daycountFraction(LocalDate start, LocalDate end) {
            long days = start.until(end, ChronoUnit.DAYS);
            return days/360.0;
        }
    },
    /**
     * Actual number of days over one year (365 days).
     */
    ACT365 {
        @Override
        public double daycountFraction(LocalDate start, LocalDate end) {
            long days = start.until(end, ChronoUnit.DAYS);
            return days/365.0;
        }
    },
    /**
     * Actual number of days over one year (365.25 days).
     */
    ACT36525 {
        @Override
        public double daycountFraction(LocalDate start, LocalDate end) {
            long days = start.until(end, ChronoUnit.DAYS);
            return days / 365.25;
        }
    },
    /**
     * Actual number of days over actual number days in years.
     * A leap year has 366 days and is taken into account in the calculation.
     */
    ACTACT {
        @Override
        public double daycountFraction(LocalDate start, LocalDate end) {
            int y1days = start.lengthOfYear();
            int y2days = end.lengthOfYear();
            int year1 = start.getYear();
            int year2 = end.getYear();
            long period1 = start.until(LocalDate.of(year1+1, 1, 1),ChronoUnit.DAYS);
            long period2 = LocalDate.of(year2,1,1).until(end, ChronoUnit.DAYS);
            return period1/(double)y1days+year2-year1-1+period2/(double)y2days;
        }
    },
    /**
     * Each month has 30 days and a year has 360 days. This is 30/360 Bond DayCountBasis.
     */
    B30360 {
        @Override
        public double daycountFraction(LocalDate start, LocalDate end) {
            int y1 = start.getYear(), m1 = start.getMonthValue(), d1 = start.getDayOfMonth();
            int y2 = end.getYear(), m2 = end.getMonthValue(), d2 = end.getDayOfMonth();
            if(d1>=30) {
                d1=30;
                if(d2>30) {
                    d2=30;
                }
            }
            return ((d2-d1)+(m2-m1)*30)/360.0+y2-y1;
        }
    };

    /**
     * Calculate the day count fraction bewteen two dates
     * @param start The first date
     * @param end The second date
     * @return Day count fraction
     */
    public abstract double daycountFraction(LocalDate start, LocalDate end);
}
