package tech.tongyu.bct.service.quantlib.market.vol.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class VolCalendarTest {
    private static LocalDateTime friday = LocalDateTime.parse("2018-04-13T00:00:00");
    private static LocalDateTime saturday = friday.plusDays(1);
    private static LocalDateTime sunday = saturday.plusDays(1);
    private static LocalDateTime holiday1 = LocalDateTime.parse("2018-04-05T00:00:00");
    private static LocalDateTime holiday2 = holiday1.plusDays(1);
    private static LocalDateTime workday = LocalDateTime.parse("2018-04-08T00:00:00");
    private static LocalDateTime bigday = LocalDateTime.parse("2018-04-20T00:00:00");
    private static double weekendWeight = 0.33;
    private static double holidayWeight = 1.0 / 7;
    private static double bigWeight = 3.14;
    private static HashMap<LocalDate, Double> specialWeight =
            new HashMap<LocalDate, Double>() {
                {
                    put(holiday1.toLocalDate(), holidayWeight);
                    put(holiday2.toLocalDate(), holidayWeight);
                    put(workday.toLocalDate(), 1.0);
                    put(bigday.toLocalDate(), bigWeight);
                }
            };
    private static VolCalendar testCalendar =
            new VolCalendar(weekendWeight, specialWeight);
    private static LocalDateTime start = LocalDateTime.parse("2018-04-01T00:00:00");
    private static LocalDateTime end = LocalDateTime.parse("2018-05-01T00:00:00");
    private static int nWork = 19;
    private static int nWeekend = 8;
    private static int nHoliday = 2;
    private static int nBig = 1;
    private static final double err = 1e-9;

    @Test
    public void none() {
        VolCalendar nonCalendar = VolCalendar.none();
        assertEquals(1, nonCalendar.getWeight(friday), err);
        assertEquals(1, nonCalendar.getWeight(saturday), err);
        assertEquals(1, nonCalendar.getWeight(sunday), err);
    }

    @Test
    public void weekendsOnly() {
        double weight = 0.5;
        VolCalendar weekendsCalendar = VolCalendar.weekendsOnly(weight);
        assertEquals(1, weekendsCalendar.getWeight(friday), err);
        assertEquals(weight, weekendsCalendar.getWeight(saturday), err);
        assertEquals(weight, weekendsCalendar.getWeight(sunday), err);
    }

    @Test
    public void getEffectiveNumDays() {
        // in one day
        assertEquals(0.5, testCalendar.getEffectiveNumDays(
                friday, friday.plusHours(12)), err);
        assertEquals(0.5 * weekendWeight, testCalendar.getEffectiveNumDays(
                saturday, saturday.plusHours(12)), err);
        assertEquals(0.5 * holidayWeight, testCalendar.getEffectiveNumDays(
                holiday1, holiday1.plusHours(12)), err);
        assertEquals(0.5, testCalendar.getEffectiveNumDays(
                workday, workday.plusHours(12)), err);
        assertEquals(0.5 * bigWeight, testCalendar.getEffectiveNumDays(
                bigday, bigday.plusHours(12)), err);
        // successive days
        assertEquals(1, testCalendar.getEffectiveNumDays(
                friday.minusHours(12), friday.plusHours(12)), err);
        assertEquals(0.5 + 0.5 * weekendWeight, testCalendar.getEffectiveNumDays(
                sunday.plusHours(12), sunday.plusHours(36)), err);
        assertEquals(0.5 * weekendWeight + 0.5, testCalendar.getEffectiveNumDays(
                workday.minusHours(12), workday.plusHours(12)), err);
        assertEquals(0.5 * holidayWeight + 0.5, testCalendar.getEffectiveNumDays(
                holiday1.minusHours(12), holiday1.plusHours(12)), err);
        // integer days
        assertEquals(5 + 2 * weekendWeight, testCalendar.getEffectiveNumDays(
                LocalDateTime.parse("2018-04-09T00:00:00"),
                LocalDateTime.parse("2018-04-16T00:00:00")), err);
        assertEquals(2 * weekendWeight + 4 + 2 * holidayWeight,
                testCalendar.getEffectiveNumDays(LocalDateTime.parse("2018-04-01T00:00:00"),
                        LocalDateTime.parse("2018-04-09T00:00:00")), err);
        assertEquals(nWork + nWeekend * weekendWeight + nHoliday * holidayWeight
                        + nBig * bigWeight, testCalendar.getEffectiveNumDays(start, end), err);
        // fraction days
        assertEquals(1.5 * holidayWeight + 10 + 3 * weekendWeight + 0.5 * bigWeight,
                testCalendar.getEffectiveNumDays(holiday1.plusHours(12), bigday.plusHours(12)),
                err);
        assertEquals(1.5 * holidayWeight + 10 + 3.5 * weekendWeight + bigWeight,
                testCalendar.getEffectiveNumDays(holiday1.plusHours(12),
                        LocalDateTime.parse("2018-04-21T12:00:00")), err);
        // end equals start
        assertEquals(0, testCalendar.getEffectiveNumDays(start, start), err);
        // end before start
        try {
            testCalendar.getEffectiveNumDays(end, start);
            fail("Expected RuntimeException thrown.");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("End time is before start time."));
        }
    }

    @Test
    public void interpVar() {
        double varStart = 0.123;
        double localVar = 1.0 / 369;
        double varEnd = varStart + localVar * (nWork + weekendWeight * nWeekend
                + holidayWeight * nHoliday + bigWeight * nBig);
        assertEquals(varStart, testCalendar.interpVar(start, start, varStart, end, varEnd), err);
        assertEquals(varEnd, testCalendar.interpVar(end, start, varStart, end, varEnd), err);
        // 1 day
        assertEquals(varStart + localVar * weekendWeight,
                testCalendar.interpVar(LocalDateTime.parse("2018-04-02T00:00:00"),
                        start, varStart, end, varEnd), err);
        // after workdays
        assertEquals(varStart + localVar * (weekendWeight + 3),
                testCalendar.interpVar(LocalDateTime.parse("2018-04-05T00:00:00"),
                        start, varStart, end, varEnd), err);
        // after holidays
        assertEquals(varStart + localVar * (weekendWeight + 3 + 1.5 * holidayWeight),
                testCalendar.interpVar(LocalDateTime.parse("2018-04-06T12:00:00"),
                        start, varStart, end, varEnd), err);
        // after big day
        assertEquals(varStart + localVar * (13 + 5 * weekendWeight + 2 * holidayWeight +
                bigWeight), testCalendar.interpVar(LocalDateTime.parse("2018-04-22T00:00:00"),
                start, varStart, end, varEnd), err);
        // out of bound
        try {
            testCalendar.interpVar(start.minusDays(1), start, varStart, end, varEnd);
            fail("Expected RuntimeException thrown.");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Required time is before start time."));
        }
//        try {
//            testCalendar.interpVar(end.plusDays(1), start, varStart, end, varEnd);
//            fail("Expected RuntimeException thrown.");
//        } catch (RuntimeException e) {
//            assertThat(e.getMessage(), is("Required time is after end time."));
//        }
    }

    @Test
    public void getWeight() {
        assertEquals(1, testCalendar.getWeight(friday), err);
        assertEquals(weekendWeight, testCalendar.getWeight(saturday), err);
        assertEquals(holidayWeight, testCalendar.getWeight(holiday1), err);
        assertEquals(bigWeight, testCalendar.getWeight(bigday), err);
    }

    @Test
    public void timeDerivative() {
        double varStart = 0.123;
        double localVar = 1.0 / 369;
        double varEnd = varStart + localVar * (nWork + weekendWeight * nWeekend
                + holidayWeight * nHoliday + bigWeight * nBig);
        assertEquals(localVar, testCalendar.timeDerivative(
                friday, start, varStart, end, varEnd), err);
        assertEquals(localVar * weekendWeight, testCalendar.timeDerivative(
                saturday, start, varStart, end, varEnd), err);
        assertEquals(localVar * holidayWeight, testCalendar.timeDerivative(
                holiday1, start, varStart, end, varEnd), err);
        assertEquals(localVar * bigWeight, testCalendar.timeDerivative(
                bigday, start, varStart, end, varEnd), err);
    }

    @Test
    public void getWeekendWeight() {
        assertEquals(weekendWeight, testCalendar.getWeekendWeight(), err);
    }

    @Test
    public void getSpecialDates() {
        Set<LocalDate> specialDates = testCalendar.getSpecialDates();
        TreeSet<LocalDate> dates = new TreeSet<>();
        dates.add(holiday1.toLocalDate());
        dates.add(holiday2.toLocalDate());
        dates.add(workday.toLocalDate());
        dates.add(bigday.toLocalDate());
        assertTrue(dates.containsAll(specialDates));
        assertTrue(specialDates.containsAll(dates));
    }

    @Test
    public void getSpecialWeights() {
        Map<LocalDate, Double> specialWeights = testCalendar.getSpecialWeights();
        Set<LocalDate> specialDates = specialWeights.keySet();
        TreeSet<LocalDate> dates = new TreeSet<>();
        dates.add(holiday1.toLocalDate());
        dates.add(holiday2.toLocalDate());
        dates.add(workday.toLocalDate());
        dates.add(bigday.toLocalDate());
        assertTrue(dates.containsAll(specialDates));
        assertTrue(specialDates.containsAll(dates));
        assertEquals(holidayWeight, specialWeights.get(holiday1.toLocalDate()), err);
        assertEquals(holidayWeight, specialWeights.get(holiday2.toLocalDate()), err);
        assertEquals(1 - weekendWeight, specialWeights.get(workday.toLocalDate()), err);
        assertEquals(bigWeight, specialWeights.get(bigday.toLocalDate()), err);
    }
}