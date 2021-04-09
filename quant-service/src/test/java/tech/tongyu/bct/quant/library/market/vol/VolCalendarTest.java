package tech.tongyu.bct.quant.library.market.vol;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.library.financial.date.DateCalcUtils;
import tech.tongyu.bct.quant.library.financial.date.Holidays;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class VolCalendarTest {
    private final String calendar = "TEST_VOLCALENDAR_HOLIDAYS";
    private final List<LocalDate> holidays = Arrays.asList(
            LocalDate.of(2019, 3, 18), // monday
            LocalDate.of(2019, 3, 20)  // wednesday
    );
    private final List<String> calendars = Arrays.asList(calendar);

    @Before
    public void setup() {
        Holidays.Instance.add(calendar, holidays);
    }

    @After
    public void tearDown() {
        Holidays.Instance.deleteAll(calendar);
    }

    @Test
    public void testNone() {
        VolCalendar none = VolCalendar.none();
        LocalDateTime start = LocalDateTime.of(2019, 3, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2019, 3, 21, 0, 0);
        double tau = none.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateTimeUtils.days(start, end), tau,
                DoubleUtils.SMALL_NUMBER);
        start = LocalDateTime.of(2019, 3, 16, 0, 0);
        tau = none.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateTimeUtils.days(start, end), tau, DoubleUtils.SMALL_NUMBER);
    }

    @Test
    public void testWeends() {
        VolCalendar weekends = VolCalendar.weekendsOnly(0.0);
        LocalDateTime start = LocalDateTime.of(2019, 3, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2019, 3, 21, 0, 0);
        List<String> weekendsOnlyCalendar = Arrays.asList("WEEKENDS");
        double tau = weekends.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, weekendsOnlyCalendar), tau,
                DoubleUtils.SMALL_NUMBER);
        start = LocalDateTime.of(2019, 3, 16, 0, 0);
        tau = weekends.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, weekendsOnlyCalendar),
                tau, DoubleUtils.SMALL_NUMBER);
        // weekend to weekend
        start = LocalDateTime.of(2019, 3, 16, 9, 0);
        end = LocalDateTime.of(2019, 3, 23, 15, 0);
        tau = weekends.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, weekendsOnlyCalendar),
                tau, DoubleUtils.SMALL_NUMBER);
    }

    @Test
    public void testHolidays() {
        VolCalendar volCalendar = VolCalendar.fromCalendars(calendars);
        // weekend to holiday
        LocalDateTime start = LocalDateTime.of(2019, 3, 16, 9, 0);
        LocalDateTime end = LocalDateTime.of(2019, 3, 18, 15, 0);
        double tau = volCalendar.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                tau, DoubleUtils.SMALL_NUMBER);
        // weekend to holiday, 1 business day in between
        end = LocalDateTime.of(2019, 3, 19, 15, 0);
        tau = volCalendar.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                tau, DoubleUtils.SMALL_NUMBER);
        // business to holiday
        start = LocalDateTime.of(2019, 3, 19, 9, 0);
        end = LocalDateTime.of(2019, 3, 20, 15, 0);
        tau = volCalendar.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                tau, DoubleUtils.SMALL_NUMBER);
        // business to business
        end = LocalDateTime.of(2019, 3, 21, 15, 0);
        tau = volCalendar.getEffectiveNumDays(start, end);
        Assert.assertEquals(DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                tau, DoubleUtils.SMALL_NUMBER);
    }
}
