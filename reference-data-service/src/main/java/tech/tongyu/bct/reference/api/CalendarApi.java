package tech.tongyu.bct.reference.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.library.financial.date.Holidays;
import tech.tongyu.bct.quant.library.market.vol.VolCalendarCache;
import tech.tongyu.bct.quant.service.QuantService;
import tech.tongyu.bct.reference.dto.TradingCalendarDTO;
import tech.tongyu.bct.reference.dto.CalendarDescriptionDTO;
import tech.tongyu.bct.reference.dto.VolCalendarDTO;
import tech.tongyu.bct.reference.service.TradingCalendarService;
import tech.tongyu.bct.reference.service.VolCalendarService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarApi {
    private TradingCalendarService tradingCalendarService;
    private VolCalendarService volCalendarService;

    @Autowired
    public CalendarApi(TradingCalendarService tradingCalendarService,
                       VolCalendarService volCalendarService) {
        this.tradingCalendarService = tradingCalendarService;
        this.volCalendarService = volCalendarService;
    }

    @BctMethodInfo(
            description = "创建交易日历",
            retDescription = "交易日历信息",
            retName = "TradingCalendarDTO",
            returnClass = TradingCalendarDTO.class,
            service = "reference-data-service"
    )
    public TradingCalendarDTO refTradingCalendarCreate(
            @BctMethodArg(name = "calendarId", description = "交易日历代码。比如'SHFE'。") String calendarId,
            @BctMethodArg(name = "calendarName", description = "交易日历名称。比如'上期所交易日历'。", required = false) String calendarName,
            @BctMethodArg(name = "holidays", description = "非交易日列表，包含日期，（可选）备注") List<Map<String, Object>> holidays
    ) {
        String name = Objects.isNull(calendarName) ? calendarId : calendarName;
        List<TradingCalendarDTO.Holiday> hs = holidays.stream()
                .map(h -> JsonUtils.mapper.convertValue(h, TradingCalendarDTO.Holiday.class))
                .collect(Collectors.toList());
        TradingCalendarDTO ret = tradingCalendarService.create(calendarId, name, hs);
        Holidays.Instance.replace(calendarId,
                ret.getHolidays().stream()
                .map(TradingCalendarDTO.Holiday::getHoliday)
                .collect(Collectors.toList()));
        return ret;
    }

    @BctMethodInfo(
            description = "按代码检索交易日历",
            retDescription = "交易日历信息",
            retName = "TradingCalendarDTO",
            returnClass = TradingCalendarDTO.class,
            service = "reference-data-service"
    )
    public TradingCalendarDTO refTradingCalendarGet(
            @BctMethodArg(name = "calendarId", description = "交易日历代码") String calendarId
    ) {
        Optional<TradingCalendarDTO> calendar = tradingCalendarService.getCalendar(calendarId);
        if (calendar.isPresent()) {
            return calendar.get();
        } else {
            throw new CustomException(ErrorCode.MISSING_ENTITY, "交易日历不存在：" + calendarId);
        }
    }

    @BctMethodInfo(
            description = "向指定交易日历中添加非交易日",
            retDescription = "非交易日信息",
            retName = "List<TradingCalendarDTO.Holiday>",
            returnClass = TradingCalendarDTO.Holiday.class,
            service = "reference-data-service"
    )
    public List<TradingCalendarDTO.Holiday> refTradingHolidaysAdd(
            @BctMethodArg(name = "calendarId", description = "交易日历代码。比如'SHFE'。") String calendarId,
            @BctMethodArg(name = "holidays", description = "非交易日列表，包含日期，（可选）备注") List<Map<String, Object>> holidays) {
        List<TradingCalendarDTO.Holiday> hs = holidays.stream()
                .map(h -> JsonUtils.mapper.convertValue(h, TradingCalendarDTO.Holiday.class))
                .collect(Collectors.toList());
        List<TradingCalendarDTO.Holiday> ret = tradingCalendarService.addHolidays(calendarId, hs);
        Holidays.Instance.mergeHolidays(calendarId,
                ret.stream()
                        .map(TradingCalendarDTO.Holiday::getHoliday)
                        .collect(Collectors.toList()));
        return ret;
    }

    @BctMethodInfo(
            description = "从指定交易日历中删除非交易日",
            retDescription = "非交易日信息",
            retName = "List<TradingCalendarDTO.Holiday>",
            returnClass = TradingCalendarDTO.Holiday.class,
            service = "reference-data-service"
    )
    public List<TradingCalendarDTO.Holiday> refTradingHolidaysDelete(
            @BctMethodArg(name = "calendarId", description = "交易日历代码。比如'SHFE'。") String calendarId,
            @BctMethodArg(name = "holidays", description = "非交易日列表，包含日期，（可选）备注", argClass = TradingCalendarDTO.Holiday.class) List<Map<String, Object>> holidays) {
        List<TradingCalendarDTO.Holiday> hs = holidays.stream()
                .map(h -> JsonUtils.mapper.convertValue(h, TradingCalendarDTO.Holiday.class))
                .collect(Collectors.toList());
        List<TradingCalendarDTO.Holiday> ret = tradingCalendarService.deleteHolidays(calendarId, hs);
        Holidays.Instance.removeHolidays(calendarId, ret.stream()
                .map(TradingCalendarDTO.Holiday::getHoliday)
                .collect(Collectors.toList()));
        return ret;
    }

    @BctMethodInfo(
            description = "罗列交易日历（仅名称）",
            retDescription = "交易日历名称",
            retName = "List<CalendarDescriptionDTO>",
            returnClass = CalendarDescriptionDTO.class,
            service = "reference-data-service"
    )
    public List<CalendarDescriptionDTO> refTradingCalendarsList() {
        return tradingCalendarService.listCalendars();
    }

    @BctMethodInfo(
            description = "创建波动率日历",
            retDescription = "波动率日历信息",
            retName = "VolCalendarDTO",
            returnClass = VolCalendarDTO.class,
            service = "reference-data-service"
    )
    public VolCalendarDTO refVolCalendarCreate(
            @BctMethodArg(name = "calendarId", description = "交易日历代码。比如'SHFE'。") String calendarId,
            @BctMethodArg(name = "calendarName", description = "交易日历名称。比如'上期所交易日历'。", required = false) String calendarName,
            @BctMethodArg(name = "weekendWeight", description = "周末vol权重", required = false) Number weekendWeight,
            @BctMethodArg(name = "specialDates", description = "波动率特殊日期", argClass = VolCalendarDTO.SpecialDate.class) List<Map<String, Object>> specialDates){
        String name = Objects.isNull(calendarName) ? calendarId : calendarName;
        double wkdWeight = Objects.isNull(weekendWeight) ? 1.0 : weekendWeight.doubleValue();
        List<VolCalendarDTO.SpecialDate> hs = specialDates.stream()
                .map(h -> JsonUtils.mapper.convertValue(h, VolCalendarDTO.SpecialDate.class))
                .collect(Collectors.toList());
        VolCalendarDTO dto = volCalendarService.create(calendarId, name, wkdWeight, hs);
        VolCalendarCache.create(dto.getCalendarId(),
                wkdWeight, hs.stream()
                        .collect(Collectors.toMap(VolCalendarDTO.SpecialDate::getSpecialDate,
                                VolCalendarDTO.SpecialDate::getWeight)));
        return dto;
    }

    @BctMethodInfo(
            description = "按代码检索波动率日历",
            retDescription = "波动率日历信息",
            retName = "VolCalendarDTO",
            returnClass = VolCalendarDTO.class,
            service = "reference-data-service"
    )
    public VolCalendarDTO refVolCalendarGet(
            @BctMethodArg(name = "calendarId", description = "波动率日历代码") String calendarId
    ) {
        Optional<VolCalendarDTO> calendar = volCalendarService.getCalendar(calendarId);
        if (calendar.isPresent()) {
            return calendar.get();
        } else {
            throw new CustomException(ErrorCode.MISSING_ENTITY, "波动率日历不存在：" + calendarId);
        }
    }

    @BctMethodInfo(
            description = "更新波动率日历周末权重",
            retDescription = "波动率日历信息",
            retName = "VolCalendarDTO",
            returnClass = VolCalendarDTO.class,
            service = "reference-data-service"
    )
    public VolCalendarDTO refVolCalendarWeekendWeightUpdate(
            @BctMethodArg(name = "calendarId", description = "波动率日历代码") String calendarId,
            @BctMethodArg(name = "weight", description = "周末权重") Number weight
    ) {
        VolCalendarDTO dto = volCalendarService.updateWeekendWeight(calendarId, weight.doubleValue());
        VolCalendarCache.create(dto.getCalendarId(),
                weight.doubleValue(), dto.getSpecialDates().stream()
                        .collect(Collectors.toMap(VolCalendarDTO.SpecialDate::getSpecialDate,
                                VolCalendarDTO.SpecialDate::getWeight)));
        return dto;
    }

    @BctMethodInfo(
            description = "向指定波动率日历中添加非交易日",
            retDescription = "波动率日历中非交易日",
            retName = "List<VolCalendarDTO.SpecialDate>",
            returnClass = VolCalendarDTO.SpecialDate.class,
            service = "reference-data-service"
    )
    public List<VolCalendarDTO.SpecialDate> refVolSpecialDatesAdd(
            @BctMethodArg(name = "calendarId",description = "波动率日历代码。比如'SHFE'。") String calendarId,
            @BctMethodArg(name = "specialDates", description = "非交易日列表，包含日期，（可选）备注", argClass = VolCalendarDTO.SpecialDate.class) List<Map<String, Object>> specialDates
    ) {
        List<VolCalendarDTO.SpecialDate> hs = specialDates.stream()
                .map(h -> JsonUtils.mapper.convertValue(h, VolCalendarDTO.SpecialDate.class))
                .collect(Collectors.toList());
        List<VolCalendarDTO.SpecialDate> ret = volCalendarService.addSpecialDates(calendarId, hs);
        volCalendarService.getCalendar(calendarId).ifPresent(dto ->
                VolCalendarCache.create(dto.getCalendarId(),
                dto.getWeekendWeight(), dto.getSpecialDates().stream()
                        .collect(Collectors.toMap(VolCalendarDTO.SpecialDate::getSpecialDate,
                                VolCalendarDTO.SpecialDate::getWeight))));
        return ret;
    }

    @BctMethodInfo(
            description = "从指定交易日历中删除非交易日",
            retDescription = "波动率日历中非交易日",
            retName = "List<VolCalendarDTO.SpecialDate>",
            returnClass = VolCalendarDTO.SpecialDate.class,
            service = "reference-data-service"
    )
    public List<VolCalendarDTO.SpecialDate> refVolSpecialDatesDelete(
            @BctMethodArg(name = "specialDateUUIDs", description = "特殊日期的UUID列表") List<String> specialDateUUIDs
    ) {
        List<VolCalendarDTO.SpecialDate> ret = volCalendarService.deleteSpecialDates(specialDateUUIDs.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList()));
        // WARNING: this is unnecessary. but since the input is uuid, there is currently no way
        // to find out which calendars these dates belong to.
        // so we just re-load every calendar into quantlib, which is not efficient
        volCalendarService.loadIntoQuantlib();
        return ret;
    }

    @BctMethodInfo(
            description = "从指定交易日历中更新非交易日",
            retDescription = "交易日历中非交易日",
            retName = "VolCalendarDTO.SpecialDate",
            returnClass = VolCalendarDTO.SpecialDate.class,
            service = "reference-data-service"
    )
    public VolCalendarDTO.SpecialDate refVolSpecialDatesUpdate(
            @BctMethodArg(name = "specialDateUUID", description = "特殊日期的UUID") String specialDateUUID,
            @BctMethodArg(name = "weight", description = "特殊日期的权重") Number weight,
            @BctMethodArg(name = "note", description = "特殊日期的note") String note
    ) {
        VolCalendarDTO.SpecialDate ret = volCalendarService.updateSpecialDate(UUID.fromString(specialDateUUID),
                weight.doubleValue(), note);
        // WARNING: this is unnecessary. but since the input is uuid, there is currently no way
        // to find out which calendars these dates belong to.
        // so we just re-load every calendar into quantlib, which is not efficient
        volCalendarService.loadIntoQuantlib();
        return ret;
    }

    @BctMethodInfo(
            description = "罗列交易日历（仅名称）",
            retDescription = "交易日历",
            retName = "List<CalendarDescriptionDTO>",
            returnClass = CalendarDescriptionDTO.class,
            service = "reference-data-service"
    )
    public List<CalendarDescriptionDTO> refVolCalendarsList() {
        return volCalendarService.listCalendars();
    }

    @BctMethodInfo(
            description = "获取第T-n交易日",
            retDescription = "交易日",
            retName = "String",
            returnClass = String.class,
            service = "reference-data-service"
    )
    public String refTradeDateByOffsetGet(
            @BctMethodArg(name = "offset", description = "偏移量，T + n") int offset,
            @BctMethodArg(name = "currentDate", description = "指定日期T做计算", required = false) String currentDate
    ) {
        return tradingCalendarService.getTradeDateByOffset(offset, currentDate);
    }
}
