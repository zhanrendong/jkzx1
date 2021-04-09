package tech.tongyu.bct.common.util;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeUtils {
    private static Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    public static Long NANOS_IN_DAY = 24 * 3600 * 1000000000L;

    public static int DAYS_IN_YEAR = 365;

    public static ZoneId BCT_DEFAULT_TIMEZONE = ZoneId.of("Asia/Shanghai");

    public static LocalDateTime BCT_MAX_LOCALDATETIME = LocalDateTime.of(3000, 1, 1,
            0, 0);

    public static LocalDateTime parseToLocalDateTime(String timestamp) {
        if (Objects.isNull(timestamp)) {
            return LocalDateTime.now();
        }
        if (p.matcher(timestamp).matches()) {
            return LocalDate.parse(timestamp).atStartOfDay();
        }
        try {
            return LocalDateTime.parse(timestamp);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "failed to parse input timestamp " + timestamp
                    + " into datetime");
        }

    }

    public static LocalDate parseToLocalDate(String timestamp) {
        if (Objects.isNull(timestamp)) {
            return LocalDate.now();
        }
        if (p.matcher(timestamp).matches()) {
            return LocalDate.parse(timestamp);
        }
        try {
            return LocalDateTime.parse(timestamp).toLocalDate();
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "failed to parse input timestamp to date "
                    + timestamp);
        }
    }

    public static ZonedDateTime parse(String timestamp, String timezone) {
        if (Objects.isNull(timestamp) && Objects.isNull(timezone)) {
            return Instant.now().atZone(BCT_DEFAULT_TIMEZONE);
        } else if (!Objects.isNull(timestamp) && Objects.isNull(timezone)) {
            return ZonedDateTime.of(parseToLocalDateTime(timestamp), BCT_DEFAULT_TIMEZONE);
        } else if (Objects.isNull(timestamp)) {
            return Instant.now().atZone(ZoneId.of(timezone));
        } else {
            return ZonedDateTime.of(parseToLocalDateTime(timestamp), ZoneId.of(timezone));
        }
    }

    public static Period parsePeriod(String tenor) {
        String normalized = tenor.trim().toUpperCase();
        try {
            return Period.parse("P" + normalized);
        } catch (Exception e) {
            throw new CustomException("无法解析期限输入： " + tenor);
        }
    }

    public static double days(LocalDateTime start, LocalDateTime end) {
        return start.until(end, ChronoUnit.NANOS) / (double) NANOS_IN_DAY;
    }

    public static double days(LocalDate start, LocalDate end) {
        return start.until(end, ChronoUnit.DAYS);
    }

    public static boolean isWeekend(LocalDate t) {
        return t.getDayOfWeek().getValue() > 5;
    }

    public static boolean isWeekend(LocalDateTime t) {
        return isWeekend(t.toLocalDate());
    }

    public static LocalDateTime ofDate(Date date){
        return LocalDateTime.ofInstant(date.toInstant(), BCT_DEFAULT_TIMEZONE);
    }
}
