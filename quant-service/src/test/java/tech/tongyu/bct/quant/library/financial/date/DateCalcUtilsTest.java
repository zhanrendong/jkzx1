package tech.tongyu.bct.quant.library.financial.date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.DoubleUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Arrays;
import java.util.List;

public class DateCalcUtilsTest {
    private final String calendar = "TEST_COUNT_BUSINESS_DAYS_CALENDAR";
    private final List<LocalDate> holidays = Arrays.asList(
            LocalDate.of(2019, 3, 18),
            LocalDate.of(2019, 3, 20)
    );
    private final List<String> calendars = Arrays.asList(calendar);

    @Before
    public void setup() {
        Holidays.Instance.add(calendar, holidays);
    }

    @After
    public void tearDown() {
        Holidays.Instance.delete(calendar);
    }

    @Test
    public void testCountBusinessDays() {
        LocalDate start = LocalDate.of(2019, 3, 15);
        LocalDate end = LocalDate.of(2019, 3, 18);
        Assert.assertEquals(0, DateCalcUtils.countBusinessDays(start, end, calendars));
        end = LocalDate.of(2019, 3, 19);
        Assert.assertEquals(1, DateCalcUtils.countBusinessDays(start, end, calendars));
        end = LocalDate.of(2019, 3, 20);
        Assert.assertEquals(1, DateCalcUtils.countBusinessDays(start, end, calendars));
        end = LocalDate.of(2019, 3, 21);
        Assert.assertEquals(2, DateCalcUtils.countBusinessDays(start, end, calendars));

        start = LocalDate.of(2019, 3, 18);
        Assert.assertEquals(2, DateCalcUtils.countBusinessDays(start, end, calendars));
    }

    @Test
    public void testCountBusinessTime() {
        LocalDateTime start = LocalDateTime.of(2019, 3, 17, 12, 0);
        LocalDateTime end = LocalDateTime.of(2019, 3, 18, 12, 0);
        Assert.assertEquals(0.0, DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                DoubleUtils.SMALL_NUMBER);

        end = LocalDateTime.of(2019, 3, 19, 12, 0);
        Assert.assertEquals(0.5, DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                DoubleUtils.SMALL_NUMBER);

        start = LocalDateTime.of(2019, 3, 15, 12, 0);
        Assert.assertEquals(1.0, DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                DoubleUtils.SMALL_NUMBER);

        start = LocalDateTime.of(2019, 3, 19, 0, 0);
        Assert.assertEquals(0.5, DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                DoubleUtils.SMALL_NUMBER);

        end = LocalDateTime.of(2019, 3, 21, 6, 0);
        Assert.assertEquals(1.25, DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                DoubleUtils.SMALL_NUMBER);

        start = LocalDateTime.of(2019, 3, 18, 21, 0);
        Assert.assertEquals(1.25, DateCalcUtils.countBusinessTimeInDays(start, end, calendars),
                DoubleUtils.SMALL_NUMBER);
    }
    @Test
    public void testTenorConversion() {
        Period period = DateTimeUtils.parsePeriod("3d");
        Assert.assertEquals(3, period.getDays());
        period = DateTimeUtils.parsePeriod("3w");
        Assert.assertEquals(21, period.getDays());
        period = DateTimeUtils.parsePeriod("3M");
        Assert.assertEquals(3, period.getMonths());
        Period oneWeek = Period.parse("P1W");
        period = DateTimeUtils.parsePeriod("1w");
        Assert.assertEquals(oneWeek.getDays(), period.getDays());
    }
}
