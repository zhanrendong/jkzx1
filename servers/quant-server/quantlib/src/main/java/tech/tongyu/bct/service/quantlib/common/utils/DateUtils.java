package tech.tongyu.bct.service.quantlib.common.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    // https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java
    public static long countWeekDays(LocalDate start, LocalDate end) {
        final DayOfWeek startWeekDay = start.getDayOfWeek().getValue() < 6 ? start.getDayOfWeek() : DayOfWeek.MONDAY;
        final DayOfWeek endWeekDay = end.getDayOfWeek().getValue() < 6 ? end.getDayOfWeek() : DayOfWeek.FRIDAY;

        final long nrOfWeeks = ChronoUnit.DAYS.between(start, end) / 7;
        final long totalWeekdDays = nrOfWeeks * 5 + Math.floorMod(endWeekDay.getValue() - startWeekDay.getValue(), 5);

        return totalWeekdDays;
    }

    // count weekends [start, end)
    // SLOW!!
    // TODO (http://10.1.2.16:8080/browse/OTMS-234) : use the trick above
    public static long countWeekends(LocalDate start, LocalDate end) {
        long n = 0;
        LocalDate d = LocalDate.from(start);
        while (d.isBefore(end)) {
            if (d.getDayOfWeek().getValue() > 5)
                n++;
            d = d.plusDays(1);
        }
        return n;
    }

    public static boolean isWeekend(LocalDate t) {
        return t.getDayOfWeek().getValue() > 5;
    }

    public static boolean isWeekend(LocalDateTime t) {
        return isWeekend(t.toLocalDate());
    }
}
