package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.library.common.EnumUtils;
import tech.tongyu.bct.quant.library.financial.date.BusinessDayAdjustment;
import tech.tongyu.bct.quant.library.financial.date.DateCalcUtils;
import tech.tongyu.bct.quant.library.financial.date.DateRollEnum;
import tech.tongyu.bct.quant.library.financial.date.Holidays;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DateCalculator {
    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlCalendarAdd(
            @BctMethodArg String calendar,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> holidays) {
        Holidays.Instance.add(calendar, holidays.stream()
                .map(DateTimeUtils::parseToLocalDate)
                .collect(Collectors.toList()));
        return calendar;
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public String qlCalendarDelete(
            @BctMethodArg String calendar
    ) {
        if (Holidays.Instance.delete(calendar))
            return "Deleted: " + calendar;
        else
            return String.format("Cannot find calendar %s", calendar);
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel})
    public boolean qlIsHoliday(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String date,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> calendars
    ) {
        return Holidays.Instance.isHoliday(DateTimeUtils.parseToLocalDate(date), calendars);
    }

    @BctMethodInfo(tags = BctApiTagEnum.Excel, excelType = BctExcelTypeEnum.ArrayDateTime)
    public List<LocalDateTime> qlHolidaysList(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> calendars) {
        return Holidays.Instance.listHolidays(calendars).stream()
                .map(t -> LocalDateTime.of(t, LocalTime.MIDNIGHT))
                .collect(Collectors.toList());
    }

    @BctMethodInfo(tags = {BctApiTagEnum.Excel}, excelType = BctExcelTypeEnum.DateTime)
    public LocalDateTime qlDateAdd(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String start,
            @BctMethodArg String tenor,
            @BctMethodArg String roll,
            @BctMethodArg String adj,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> holidays
    ) {
        LocalDate startDate = DateTimeUtils.parseToLocalDate(start);
        Period period;
        try {
            period = Period.parse("P" + tenor.toUpperCase());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("quantlib: 无法解析期限 %s", tenor));
        }
        LocalDate end = DateCalcUtils.add(startDate, period,
                DateRollEnum.valueOf(roll.toUpperCase()),
                BusinessDayAdjustment.valueOf(adj.toUpperCase()), holidays);
        return LocalDateTime.of(end, LocalTime.MIDNIGHT);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.ArrayDateTime, tags = {BctApiTagEnum.Excel})
    public List<LocalDate> qlDateScheduleCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String start,
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String end,
            @BctMethodArg String freq,
            @BctMethodArg String roll,
            @BctMethodArg String adj,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> holidays
    ) {
        LocalDate startDate = DateTimeUtils.parseToLocalDate(start);
        DateRollEnum rollEnum = EnumUtils.fromString(roll, DateRollEnum.class);
        BusinessDayAdjustment adjustment = EnumUtils.fromString(adj, BusinessDayAdjustment.class);
        Period fq;
        try {
            fq = DateTimeUtils.parsePeriod(freq);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("quantlib: 无法识别频率 %s", freq));
        }
        return DateCalcUtils.generateSchedule(startDate, DateTimeUtils.parseToLocalDate(end),
                fq, rollEnum, adjustment, holidays);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.ArrayDateTime, tags = {BctApiTagEnum.Excel})
    public List<LocalDate> qlDateScheduleGenerate(
            @BctMethodArg(excelType = BctExcelTypeEnum.DateTime) String start,
            @BctMethodArg String tenor,
            @BctMethodArg String freq,
            @BctMethodArg String roll,
            @BctMethodArg String adj,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayString) List<String> holidays
    ) {
        LocalDate startDate = DateTimeUtils.parseToLocalDate(start);
        DateRollEnum rollEnum = EnumUtils.fromString(roll, DateRollEnum.class);
        BusinessDayAdjustment adjustment = EnumUtils.fromString(adj, BusinessDayAdjustment.class);
        Period tn, fq;
        try {
            tn = DateTimeUtils.parsePeriod(tenor);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("quantlib: 无法识别期限：%s", tenor));
        }
        try {
            fq = DateTimeUtils.parsePeriod(freq);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("quantlib: 无法识别频率 %s", freq));
        }
        return DateCalcUtils.generateSchedule(startDate, tn, fq, rollEnum, adjustment, holidays);
    }
}
