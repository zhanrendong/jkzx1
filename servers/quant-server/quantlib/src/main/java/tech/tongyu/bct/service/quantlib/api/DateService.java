package tech.tongyu.bct.service.quantlib.api;

import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApiArg;
import tech.tongyu.bct.service.quantlib.financial.dateservice.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DateService {
    @BctQuantApi2(
            name = "qlCalendarAdd",
            description = "Add a holiday calendar to the system",
            args = {
                    @BctQuantApiArg(name = "city", type = "String", description = "Calendar name (usually a city name)"),
                    @BctQuantApiArg(name = "holidays", type = "ArrayDateTime", description = "A list of holidays")
            },
            retName = "calendar",
            retDescription = "A new holiday calendar",
            retType = "String"
    )
    public static String addCalendar(String city, LocalDateTime[] holidays) throws Exception {
        LocalDate[] hs = new LocalDate[holidays.length];
        for (int i = 0; i < hs.length; ++i) {
            hs[i] = holidays[i].toLocalDate();
        }
        Holidays.Instance.add(city, hs);
        return city;
    }

    @BctQuantApi2(
            name = "qlCalendarDelete",
            description = "Delete a calendar by name",
            args = {
                    @BctQuantApiArg(name = "calendar", type = "String", description = "Name of the calendar to delete")
            },
            retName = "status",
            retType = "String",
            retDescription = "Whether the calendar has been deleted"
    )
    public static String deleteCalendar(String city) {
        if (Holidays.Instance.delete(city))
            return "Deleted: " + city;
        else
            return "Cannot find calendar {}" + city;
    }

    @BctQuantApi2(
            name = "qlCalendarsList",
            description = "List available calendars in the system",
            args = {},
            retName = "calendars",
            retDescription = "Holiday calendars",
            retType = "ArrayString"
    )
    public static String[] listCalendars() {
        return Holidays.Instance.listCalendars();
    }

    @BctQuantApi2(
            name = "qlHolidaysList",
            description = "List holidays given a set of calendars",
            args = {
                    @BctQuantApiArg(
                            name = "calendars",
                            description = "A list of calendars",
                            type = "ArrayString"
                    )
            },
            retName = "Holidays",
            retType = "ArrayDateTime",
            retDescription = "A list of holidays common to the given calendars"
    )
    public static LocalDateTime[] listHolidays(String[] centers) {
        Set<LocalDate> holidays = Holidays.Instance.listHolidays(centers);
        List<LocalDate> hs = new ArrayList<>(holidays);
        Collections.sort(hs);
        LocalDateTime[] ret = new LocalDateTime[hs.size()];
        for (int i = 0 ; i < hs.size(); ++i) {
            ret[i] = LocalDateTime.of(hs.get(i), LocalTime.MIDNIGHT);
        }
        return ret;
    }

    @BctQuantApi(
            name = "qlIsHoliday",
            description = "Check if a date is a holiday",
            argNames = {"date", "cities"},
            argTypes = {"DateTime", "ArrayString"},
            argDescriptions = {"The date to check", "A list of calendars to check against"},
            retDescription = "Whether the date is a holiday",
            retName = "",
            retType = "Boolean"
    )
    public static boolean isHoliday(LocalDateTime date, String[] cities) throws Exception {
        return Holidays.Instance.isHoliday(date.toLocalDate(), cities, true);
    }

    @BctQuantApi(
            name = "qlDateAdd",
            description = "Add a tenor to a start date and adjust by business day rules",
            argNames = {"start", "tenor", "roll", "adj", "calendars"},
            argDescriptions = {"Start date", "Tenor", "Roll convention",
                    "Business day adjustment convention", "Holiday calendars"},
            argTypes = {"DateTime", "String", "Enum", "Enum", "ArrayString"},
            retDescription = "The end date",
            retType = "DateTime",
            retName = "endDate"
    )
    public static LocalDateTime add(LocalDateTime start, String tenor,
                                    Roll roll, BusDayAdj adj, String[] calendars) {
        Period tn = Period.parse("P" + tenor.toUpperCase());
        LocalDate end = Utils.add(start.toLocalDate(), tn, roll, adj, calendars);
        return LocalDateTime.of(end, LocalTime.MIDNIGHT);
    }

    @BctQuantApi(
            name = "qlScheduleCreate",
            description = "Create a schedule from a start date, a tenor and a frequency",
            argNames = {"start", "tenor", "frequency", "roll", "adj", "calendars"},
            argDescriptions = {"Schedule start date", "Schedule length", "Frequency", "Roll convention",
                    "Business day adjustment convention", "Holiday calendars"},
            argTypes = {"DateTime", "String", "String", "Enum", "Enum", "ArrayString"},
            retDescription = "A date schedule",
            retType = "ArrayDateTime",
            retName = "schedule"
    )
    public static LocalDateTime[] genSchedule(LocalDateTime start, String tenor, String freq,
                                              Roll roll, BusDayAdj adj, String[] calendars) {
        Period tn = Period.parse("P" + tenor.toUpperCase());
        Period fq = Period.parse("P" + freq.toUpperCase());
        ArrayList<LocalDate> dates = Schedule.generate(start.toLocalDate(), tn, fq, roll, adj, calendars);
        LocalDateTime[] ret = new LocalDateTime[dates.size()];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = LocalDateTime.of(dates.get(i), LocalTime.MIDNIGHT);
        }
        return ret;
    }

    @BctQuantApi2(
            name = "qlScheduleGenerate",
            description = "Create a schedule from a start date, and end date and a frequency",
            args = {
                    @BctQuantApiArg(name = "start", type = "DateTime", description = "Schedule start date"),
                    @BctQuantApiArg(name = "end", type = "DateTime", description = "Schedule end date"),
                    @BctQuantApiArg(name = "frequency", type = "String", description = "Frequency"),
                    @BctQuantApiArg(name = "roll", type = "Enum", description = "Roll convention"),
                    @BctQuantApiArg(name = "adj", type = "Enum", description = "Business day adjustment convention"),
                    @BctQuantApiArg(name = "calendars", type = "ArrayString", description = "Holiday calendars")
            },
            retType = "ArrayDateTime",
            retDescription = "A schedule",
            retName = "schedule"
    )
    public static LocalDateTime[] generateSchedule(LocalDateTime start, LocalDateTime end, String freq,
                                              Roll roll, BusDayAdj adj, String[] calendars) {
        Period fq = Period.parse("P" + freq.toUpperCase());
        ArrayList<LocalDate> dates = Schedule.generate(start.toLocalDate(), end.toLocalDate(),
                fq, roll, adj, calendars);
        LocalDateTime[] ret = new LocalDateTime[dates.size()];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = LocalDateTime.of(dates.get(i), LocalTime.MIDNIGHT);
        }
        return ret;
    }
}