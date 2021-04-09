package tech.tongyu.bct.service.quantlib.financial.dateservice;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;

/**
 * Date utilities
 * Created by lu on 3/15/17.
 */
public class Utils {
    /**
     * Check if the input date is end of month. This is used when the roll rule is EOM.
     * @param t The date to check
     * @return TRUE or FALSE
     */
    static boolean isDayEom(LocalDate t) {
        return t.getDayOfMonth() == t.lengthOfMonth();
    }

    /**
     * Add a tenor to a given start date and do not adjust by business day rules.
     * @param start The start date
     * @param tenor The tenor to add, e.g. 1M/2M/1Y ...
     * @param roll Forward or backward (including possibly EOM)
     * @return
     */
    public static LocalDate getUnAdjEndDate(LocalDate start, Period tenor, Roll roll) {
        LocalDate end = start.plus(tenor);
        if (tenor.getDays() == 0 && isDayEom(start)
                && (roll == Roll.BACKWARD_EOM || roll == roll.FORWARD_EOM)) {
            return end.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            return end;
        }
    }

    /**
     * Add a tenor to a start date. For example, add 1M to 2017-03-15.
     * @param start The start date
     * @param tenor The tenor to add
     * @param roll Roll rule. See {@link Roll}
     * @param adj Business day adjustment convention. See {@link BusDayAdj}
     * @param calendars Holiday calendars for business day adjustment. An empty input means weekends only.
     * @return
     */
    public static LocalDate add(LocalDate start, Period tenor, Roll roll, BusDayAdj adj, String[] calendars) {
        // get end date
        LocalDate end = getUnAdjEndDate(start, tenor, roll);
        return adj.adjust(end, calendars);
    }
}
