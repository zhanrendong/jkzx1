package tech.tongyu.bct.quant.library.financial.date;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * BUSINESS day adjustment
 * <p>
 * When generating a date schedule, if a generated day falls on a holiday or weekends
 * usually the day will be adjusted by a calendar to a good business day. Common ones are
 * <ul>
 * <li>{@link #NONE}</li>
 * <li>{@link #FOLLOWING}</li>
 * <li>{@link #PRECEDING}</li>
 * <li>{@link #MODIFIED_FOLLOWING}</li>
 * <li>{@link #MODIFIED_PRECEDING}</li>
 * </ul>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Date_rolling">Wiki: Date Rolling</a>
 */
public enum BusinessDayAdjustment {
    /**
     * No adjustment
     */
    NONE {
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars) {
            return unAdjDate;
        }
    },
    /**
     * Adjust to the next good business day
     */
    FOLLOWING {
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars) {
            LocalDate bus = unAdjDate;
            while (Holidays.Instance.isHoliday(bus, calendars)) {
                bus = bus.plusDays(1);
            }
            return bus;
        }
    },
    /**
     * Adjust to the previous good business day
     */
    PRECEDING {
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars) {
            LocalDate bus = unAdjDate;
            while (Holidays.Instance.isHoliday(bus, calendars)) {
                bus = bus.minusDays(1);
            }
            return bus;
        }
    },
    /**
     * First adjust with FOLLOWING, if the adjusted date falls in the next calendar month,
     * then roll it backward to the preceding business day.
     *
     * @see <a href="http://www.nasdaq.com/investing/glossary/m/modified-following-businessday-convention">NASDAQ</a>
     */
    MODIFIED_FOLLOWING {
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars) {
            LocalDate bus = FOLLOWING.adjust(unAdjDate, calendars);
            if (bus.getMonth() != unAdjDate.getMonth()) {
                bus = PRECEDING.adjust(unAdjDate.with(TemporalAdjusters.lastDayOfMonth()), calendars);
            }
            return bus;
        }
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars, LocalDate start, LocalDate end) {
            LocalDate bus = adjust(unAdjDate, calendars);
            if (bus.isAfter(end)) {
                bus = PRECEDING.adjust(unAdjDate, calendars);
            }
            return bus;
        }
    },
    /**
     * First adjust with PRECEDING, if the adjusted date falls in the previous calendar month,
     * then roll it forward to the preceding business day.
     */
    MODIFIED_PRECEDING {
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars) {
            LocalDate bus = PRECEDING.adjust(unAdjDate, calendars);
            if (bus.getMonth() != unAdjDate.getMonth()) {
                bus = FOLLOWING.adjust(unAdjDate.with(TemporalAdjusters.lastDayOfMonth()), calendars);
            }
            return bus;
        }
        @Override
        public LocalDate adjust(LocalDate unAdjDate, List<String> calendars, LocalDate start, LocalDate end) {
            LocalDate bus = adjust(unAdjDate, calendars);
            if (bus.isBefore(start)) {
                bus = FOLLOWING.adjust(unAdjDate, calendars);
            }
            return bus;
        }
    };

    public abstract LocalDate adjust(LocalDate unAdjDate, List<String> calendars);

    public LocalDate adjust(LocalDate unAdjDate, List<String> calendars, LocalDate start, LocalDate end) {
        return adjust(unAdjDate, calendars);
    }

}
