package tech.tongyu.bct.quant.library.financial.date;

import tech.tongyu.bct.common.util.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

/**
 * Date utilities
 * Created by lu on 3/15/17.
 */
public class DateCalcUtils {
    /**
     * Check if the input date is end of month. This is used when the roll rule is EOM.
     * @param t The date to check
     * @return TRUE or FALSE
     */
    public static boolean isDayEom(LocalDate t) {
        return t.getDayOfMonth() == t.lengthOfMonth();
    }

    /**
     * Add a tenor to a given start date and do not adjust by business day rules.
     * @param start The start date
     * @param tenor The tenor to add, e.g. 1M/2M/1Y ...
     * @param roll Forward or backward (including possibly EOM)
     * @return Unadjusted end date
     */
    public static LocalDate getUnAdjEndDate(LocalDate start, Period tenor, DateRollEnum roll) {
        if (roll == DateRollEnum.BACKWARD || roll == DateRollEnum.BACKWARD_EOM) {
            tenor = tenor.negated();
        }
        LocalDate end = start.plus(tenor);
        if (tenor.getDays() == 0 && isDayEom(start)
                && (roll == DateRollEnum.BACKWARD_EOM || roll == roll.FORWARD_EOM)) {
            return end.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            return end;
        }
    }

    /**
     * Add a tenor to a start date. For example, add 1M to 2017-03-15.
     * @param start The start date
     * @param tenor The tenor to add
     * @param roll Roll rule. See {@link DateRollEnum}
     * @param adj BUSINESS day adjustment convention. See {@link BusinessDayAdjustment}
     * @param calendars Holiday calendars for business day adjustment. An empty input means weekends only.
     * @return Adjusted end date
     */
    public static LocalDate add(LocalDate start, Period tenor, DateRollEnum roll, BusinessDayAdjustment adj,
                                List<String> calendars) {
        // get end date
        LocalDate end = getUnAdjEndDate(start, tenor, roll);
        return adj.adjust(end, calendars);
    }

    /**
     * Generate a schedule from a start date and an end date.
     * CAUTION: End date usually should be unadjusted. Otherwise rolling can easily
     * cause stub periods either at the front or back.
     * @param start Schedule start date
     * @param end Schedule end date
     * @param freq Rolling frequency
     * @param roll Rolling convention
     * @param adj Business day adjustment convention
     * @param calendars Business holidays
     * @return Generated schedule as a list of dates
     */
    public static List<LocalDate> generateSchedule(LocalDate start, LocalDate end,
                                                Period freq, DateRollEnum roll, BusinessDayAdjustment adj,
                                                List<String> calendars) {
        boolean adjToEom = (roll == DateRollEnum.BACKWARD_EOM || roll == DateRollEnum.FORWARD_EOM) && isDayEom(start);
        //   unit
        TemporalUnit unit;
        if (freq.get(ChronoUnit.YEARS) > 0)
            unit = ChronoUnit.YEARS;
        else if (freq.get(ChronoUnit.MONTHS) > 0)
            unit = ChronoUnit.MONTHS;
        else
            unit = ChronoUnit.DAYS;
        long q = freq.get(unit);
        // temporary schedule
        TreeSet<LocalDate> dates = new TreeSet<>();
        // roll forward or backward
        if (roll == DateRollEnum.BACKWARD || roll == DateRollEnum.BACKWARD_EOM) {
            LocalDate t = end;
            int c = 1;
            while (t.isAfter(start)) {
                dates.add(t);
                t = end.minus(c * q, unit);
                if (adjToEom)
                    t = t.with(TemporalAdjusters.lastDayOfMonth());
                ++c;
            }
            dates.add(start);
        } else {
            LocalDate t = start;
            int c = 1;
            while (t.isBefore(end)) {
                dates.add(t);
                t = start.plus(c * q, unit);
                if (adjToEom)
                    t = t.with(TemporalAdjusters.lastDayOfMonth());
                ++c;
            }
            dates.add(end);
        }
        // adjust by business days
        TreeSet<LocalDate> busDays = new TreeSet<>();
        for (LocalDate t : dates) {
            LocalDate b = adj.adjust(t, calendars, start, end);
            busDays.add(b);
        }
        // create the schedule
        return new ArrayList<>(busDays);
    }
    /**
     * Generate a schedule given a start date and a tenor. There are two cases to consider:
     * <p> The start date is EOM and the roll is also EOM. The end date is calculated by
     * adding the tenor to the start date and then adjust to its month end date. Then we roll
     * from the start date (FORWARD_EOM) or from the end date (BACKWARD_EOM) with the given
     * frequency to generate the rest of the unadjusted schedule dates. These dates are all
     * adjusted to month end.
     * <p> The start date is not month end or the roll is not EOM.
     * Then the unadjusted end date is simply start date plus the tenor. The rest of the schedule
     * is generated by rolling from the front (FORWARD or FORWARD_EOM) or back (BACKWARD or BACKWARD_EOM).
     * These dates are not adjusted by EOM.
     *
     * Finally the schedule is adjusted by business day adjustment rules to make sure all of them
     * are good business days.
     * @param start The start date of the schedule
     * @param tenor The tenor of the schedule. For example 6M, 1Y etc.
     * @param freq Roll frequency. For example 1M, 3M, 6M, 1Y etc.
     * @param roll Roll convention. See {@link DateRollEnum}
     * @param adj Business day adjustment convention. See {@link BusinessDayAdjustment}
     * @param calendars Holiday calendars for business day adjustment. An empty input means weekends only.
     * @return A generated schedule
     */
    public static List<LocalDate> generateSchedule(LocalDate start, Period tenor,
                                                Period freq, DateRollEnum roll, BusinessDayAdjustment adj,
                                                List<String> calendars) {
        // get end date
        LocalDate end = getUnAdjEndDate(start, tenor, roll);
        return generateSchedule(start, end, freq, roll, adj, calendars);
    }

    /**
     * Count number of business days in between two dates.
     * Important: the start is exclusive, end inclusive
     * WARNING: this is a very slow version. there is a LOT of room for improvement.
     * @param start Start date, exclusive
     * @param end End date, inclusive
     * @param holidays Holiday calendars
     * @return Number of business days in between
     */
    public static long countBusinessDays(LocalDate start, LocalDate end, List<String> holidays) {
        if (end.isBefore(start)) {
            return -countBusinessDays(end, start, holidays);
        }

        long numOfDaysBetween = ChronoUnit.DAYS.between(start, end);
        return IntStream.iterate(1, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> start.plusDays(i))
                .filter(t -> !Holidays.Instance.isHoliday(t, holidays))
                .count();
    }

    /**
     * Get business time between two points in time.
     * Business time is defined as time in business days.
     * Its performance is determined by countBusinessDays function.
     * @param start Start time
     * @param end End time
     * @param holidays holidays
     * @return Business time in days
     */
    public static double countBusinessTimeInDays(LocalDateTime start, LocalDateTime end, List<String> holidays) {
        if (end.isBefore(start)) {
            return -countBusinessTimeInDays(end, start, holidays);
        }
        // speical case: same day
        if (end.toLocalDate().equals(start.toLocalDate())) {
            return Holidays.Instance.isHoliday(end.toLocalDate(), holidays) ? 0.0
                    : DateTimeUtils.days(start, end);
        }
        double businessTime = 0.0;
        // portion in start date
        if (!Holidays.Instance.isHoliday(start.toLocalDate(), holidays)) {
            businessTime += 1.0 - start.toLocalTime().toNanoOfDay() / (double) DateTimeUtils.NANOS_IN_DAY;
        }
        // in between whole days
        long numBusinessDays = countBusinessDays(start.toLocalDate(), end.toLocalDate(), holidays);
        // portion in end date
        if (!Holidays.Instance.isHoliday(end.toLocalDate(), holidays)) {
            businessTime += end.toLocalTime().toNanoOfDay() / (double) DateTimeUtils.NANOS_IN_DAY;
            numBusinessDays--;
        }
        return businessTime + (double)numBusinessDays;
    }
}
