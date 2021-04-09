package tech.tongyu.bct.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class TimeUtils {

    public static class InvalidTimestampException extends RuntimeException {

        private static final String format = "Not valid timestamp format => {%s}, `yyyy-MM-dd` or `yyyy-MM-dd HH:mm:ss` or `yyyy-MM-dd'T'HH:mm:ss'Z'` can be acceptted.";
        private static final String format2 = "Can't match date {%s} with format string {%s}.";

        public InvalidTimestampException(String dateStr){
            super(String.format(format, dateStr));
        }

        public InvalidTimestampException(String dateStr, String formatStr){
            super(String.format(format2, dateStr, formatStr));
        }
    }

    public static Timestamp getMaxTimestamp(){
        return getTimestamp("2999-12-31T00:00:00", TimeUtils.DEFAULT_TIMEZONE);
    }

    private final static String[] formats = new String[]{
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "HH:mm:ss",
            "yyyyMMdd"
    };

    public final static String FIRST_SECOND_STRING = "00:00:00";
    public final static String LAST_SECOND_STRING = "23:59:59";
    public final static String FIFTEEN_OCLOCK = "15:00:00";
    public final static String SIXTEEN_OCLOCK = "16:00:00";
    public final static String SEVEN_OCLOCK = "07:00:00";
    public final static String EIGHT_OCLOCK = "08:00:00";
    public final static String SPACE = " ";
    public final static String DEFAULT_TIME_ZONE_STRING = "Asia/Shanghai";
    public final static String UTC_TIME_ZONE_STRING = "UTC";

    public static final TimeZone UTC = TimeZone.getTimeZone(UTC_TIME_ZONE_STRING);
    public static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(DEFAULT_TIME_ZONE_STRING);

    public static Timestamp getTimestampAfter24Hours(Timestamp startTimestamp){
        return Timestamp.from(DateUtils.addDays(startTimestamp, 1).toInstant());
    }

    public static Boolean isDateBetween(LocalDate date, LocalDate startDate, LocalDate endDate){
        return Objects.nonNull(date) && (date.equals(startDate) || date.equals(endDate)
                || (date.isAfter(startDate) && date.isBefore(endDate)));
    }

    public static LocalDateTime instantTransToLocalDateTime(Instant instant){
        return Objects.isNull(instant) ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private final static DateFormat[] dateFormats = new SimpleDateFormat[]{
            new SimpleDateFormat(formats[0]),
            new SimpleDateFormat(formats[1]),
            new SimpleDateFormat(formats[2]),
            new SimpleDateFormat(formats[3]),
            new SimpleDateFormat(formats[4]),
            new SimpleDateFormat(formats[5]),
            new SimpleDateFormat(formats[6])
    };

    public static TimeZone getTimezone(String timezoneStr, TimeZone def){
        if(StringUtils.isBlank(timezoneStr)) return def;
        return TimeZone.getTimeZone(timezoneStr);
    }

    public static TimeZone getTimezone(String timezoneStr){
        return getTimezone(timezoneStr, DEFAULT_TIMEZONE);
    }

    public static String getSimpleTimestampStr(){
        return dateFormats[3].format(new Date());
    }

    public static String getSimpleDateStr(){
        return dateFormats[4].format(new Date());
    }

    public static String getSimpleTimeStr() { return dateFormats[5].format(new Date()); }

    public static String getDateStr(Timestamp tsp, TimeZone timezone){
        if(Objects.isNull(timezone)) throw new IllegalArgumentException("null timezone");
        synchronized (dateFormats[4]){
            dateFormats[4].setTimeZone(timezone);
            String result = dateFormats[4].format(tsp);
            dateFormats[4].setTimeZone(DEFAULT_TIMEZONE);
            return result;
        }
    }

    public static String getTimeStr(Timestamp tsp, TimeZone timezone){
        if(Objects.isNull(timezone)) throw new IllegalArgumentException("null timezone");
        synchronized (dateFormats[5]){
            dateFormats[5].setTimeZone(timezone);
            String result = dateFormats[5].format(tsp);
            dateFormats[5].setTimeZone(DEFAULT_TIMEZONE);
            return result;
        }
    }

    public static Timestamp getTimestamp(String time, TimeZone timeZone, String dateFormat){
        if(StringUtils.isBlank(time)
                || Objects.isNull(timeZone)
                || StringUtils.isBlank(dateFormat)) throw new IllegalArgumentException();
        try {
            DateFormat df = new SimpleDateFormat(dateFormat);
            df.setTimeZone(timeZone);
            return new Timestamp(df.parse(time).getTime());
        }catch (ParseException e){
            throw new InvalidTimestampException(time, dateFormat);
        }
    }

    public static Timestamp getTimestamp(String time, TimeZone timeZone){
        if(StringUtils.isBlank(time))
            return getTimestamp();
        for(int i = 0 ; i < dateFormats.length; i++){
            try{
                return parse(time, i, timeZone);
            }catch (ParseException e){
                // do nothing, continue
            }
        }
        throw new InvalidTimestampException(time);
    }

    public static Timestamp getTimestamp(){
        return new Timestamp(new Date().getTime());
    }

    private static Timestamp parse(String time, int formatType, TimeZone timeZone) throws ParseException{
        if(StringUtils.isBlank(time)
                || formatType >= formats.length
                || Objects.isNull(timeZone)) throw new IllegalArgumentException();
        synchronized (dateFormats[formatType]) {
            dateFormats[formatType].setTimeZone(timeZone);
            Date result = dateFormats[formatType].parse(time);
            dateFormats[formatType].setTimeZone(DEFAULT_TIMEZONE);
            return new Timestamp(result.getTime());
        }
    }

    public static Timestamp getTimestamp(String date, String time, TimeZone timeZone){
        final int formatType = 3;
        if(StringUtils.isBlank(date)
                || Objects.isNull(timeZone)) throw new IllegalArgumentException();
        time = StringUtils.isBlank(time) ? FIRST_SECOND_STRING : time;
        String completeTime = date + SPACE + time;
        try {
            return parse(completeTime, formatType, timeZone);
        }catch (ParseException e){
            throw new InvalidTimestampException(completeTime, formats[formatType]);
        }
    }

    public static String getTimeFormatString(String format, Timestamp tsp, TimeZone timeZone){
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(tsp);
    }

    public static String getTimeFormatString(Timestamp tsp, TimeZone timeZone){
        final int formatType = 2;
        synchronized (dateFormats[formatType]){
            dateFormats[formatType].setTimeZone(timeZone);
            String dateStr = dateFormats[formatType].format(tsp);
            dateFormats[formatType].setTimeZone(DEFAULT_TIMEZONE);
            return dateStr;
        }
    }

    public static int dateDiff(Date date1, Date date2){
        return (int) ((date1.getTime() - date2.getTime()) / (1000*3600*24));
    }

    public static boolean isSameOrBefore(String time, TimeZone timeZone){
        Timestamp tsp = getTimestamp(time, timeZone);
        return isSameOrBefore(tsp);
    }

    public static boolean isSameOrBefore(Timestamp tsp){
        Timestamp now = getTimestamp();
        return tsp.before(now) || tsp.equals(now);
    }

    public static String getUTCTimestampString(String dateStr, String timeStr, TimeZone timeZone){
        Timestamp tsp = getTimestamp(dateStr, timeStr, timeZone);
        return getTimeFormatString(tsp, UTC);
    }

    public static String getUTCTimestampString(Timestamp timestamp){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(UTC);
        return dateFormat.format(timestamp);
    }

    public static Timestamp getLastBusinessDate(Timestamp valDate, TimeZone timezone){
        ZoneId zoneId = ZoneId.of(timezone.getID());
        LocalDate date = valDate.toInstant().atZone(zoneId).toLocalDate();

        if(isMonday(valDate, timezone)){
            ZonedDateTime zdt = date.minusDays(3).atStartOfDay(zoneId);
            return new Timestamp(Date.from(zdt.toInstant()).getTime());
        }

        if(isSunday(valDate, timezone)){
            ZonedDateTime zdt = date.minusDays(2).atStartOfDay(zoneId);
            return new Timestamp(Date.from(zdt.toInstant()).getTime());
        }

        ZonedDateTime zdt = date.minusDays(1).atStartOfDay(zoneId);
        return new Timestamp(Date.from(zdt.toInstant()).getTime());
    }

    public static Timestamp getStartOfDay(Timestamp valDate, TimeZone timezone){
        ZoneId zoneId = ZoneId.of(timezone.getID());
        LocalDate date = valDate.toInstant().atZone(zoneId).toLocalDate();
        ZonedDateTime zdt = date.atStartOfDay(zoneId);
        return new Timestamp(Date.from(zdt.toInstant()).getTime());
    }

    public static Boolean isWeekend(Timestamp valDate, TimeZone timezone){
        Calendar cal = Calendar.getInstance(timezone);
        cal.setTime(valDate);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return  day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    public static Boolean isMonday(Timestamp valDate, TimeZone timezone){
        Calendar cal = Calendar.getInstance(timezone);
        cal.setTime(valDate);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return  day == Calendar.MONDAY;
    }

    public static Boolean isSaturday(Timestamp valDate, TimeZone timezone){
        Calendar cal = Calendar.getInstance(timezone);
        cal.setTime(valDate);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return  day == Calendar.SATURDAY;
    }

    public static Boolean isSunday(Timestamp valDate, TimeZone timezone){
        Calendar cal = Calendar.getInstance(timezone);
        cal.setTime(valDate);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SUNDAY;
    }

}
