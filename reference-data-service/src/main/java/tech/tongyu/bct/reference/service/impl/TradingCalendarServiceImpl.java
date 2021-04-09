package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.quant.library.financial.date.Holidays;
import tech.tongyu.bct.reference.dao.dbo.TradingCalendar;
import tech.tongyu.bct.reference.dao.dbo.TradingHoliday;
import tech.tongyu.bct.reference.dao.repl.intel.TradingCalendarRepo;
import tech.tongyu.bct.reference.dao.repl.intel.TradingHolidayRepo;
import tech.tongyu.bct.reference.dto.TradingCalendarDTO;
import tech.tongyu.bct.reference.dto.CalendarDescriptionDTO;
import tech.tongyu.bct.reference.service.TradingCalendarService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TradingCalendarServiceImpl implements TradingCalendarService {
    private static Logger logger = LoggerFactory.getLogger(TradingCalendarServiceImpl.class);

    private TradingCalendarRepo tradingCalendarRepo;
    private TradingHolidayRepo tradingHolidayRepo;

    @Autowired
    public TradingCalendarServiceImpl(TradingCalendarRepo tradingCalendarRepo, TradingHolidayRepo tradingHolidayRepo) {
        this.tradingCalendarRepo = tradingCalendarRepo;
        this.tradingHolidayRepo = tradingHolidayRepo;
    }

    private TradingCalendarDTO.Holiday convert(TradingHoliday holiday) {
        return new TradingCalendarDTO.Holiday(holiday.getUuid(), holiday.getHoliday(), holiday.getNote());
    }

    private TradingHoliday convert (String calendarId, TradingCalendarDTO.Holiday holiday) {
        return new TradingHoliday(calendarId, holiday.getHoliday(), holiday.getNote());
    }

    private TradingCalendarDTO convert(TradingCalendar tradingCalendar, List<TradingHoliday> tradingHolidays) {
        return new TradingCalendarDTO(tradingCalendar.getUuid(),
                tradingCalendar.getCalendarId(), tradingCalendar.getCalendarName(),
                tradingHolidays.stream().map(this::convert).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public TradingCalendarDTO create(String calendarId, String calendarName,
                                     List<TradingCalendarDTO.Holiday> holidays) {

        TradingCalendar tradingCalendar = new TradingCalendar(calendarId, calendarName);
        List<TradingHoliday> tradingHolidays = holidays.stream()
                .map(h -> new TradingHoliday(calendarId, h.getHoliday(), h.getNote()))
                .collect(Collectors.toList());
        TradingCalendar savedCalendar = tradingCalendarRepo.save(tradingCalendar);
        List<TradingHoliday> savedHolidays = tradingHolidayRepo.saveAll(tradingHolidays);
        return convert(savedCalendar, savedHolidays);
    }

    @Override
    public Optional<TradingCalendarDTO> getCalendar(String calendarId) {
        Optional<TradingCalendar> tradingCalendar = tradingCalendarRepo.findByCalendarId(calendarId);
        if (!tradingCalendar.isPresent())
            return Optional.empty();
        List<TradingHoliday> tradingHolidays = tradingHolidayRepo.findAllByCalendarId(calendarId);
        return Optional.of(convert(tradingCalendar.get(), tradingHolidays));
    }

    @Override
    @Transactional
    public List<TradingCalendarDTO.Holiday> addHolidays(String calendarId,
                                                        List<TradingCalendarDTO.Holiday> holidays) {
        Optional<TradingCalendarDTO> calendar = getCalendar(calendarId);
        if (calendar.isPresent()) {
            Map<LocalDate, TradingCalendarDTO.Holiday> input = holidays.stream()
                    .collect(Collectors.toMap(
                            TradingCalendarDTO.Holiday::getHoliday,
                            i -> i
                    ));
            input.keySet().removeAll(calendar.get().getHolidays()
                    .stream()
                    .map(TradingCalendarDTO.Holiday::getHoliday)
                    .collect(Collectors.toList()));
            List<TradingHoliday> ths = input.values().stream()
                    .map(i -> convert(calendarId, i))
                    .collect(Collectors.toList());
            List<TradingHoliday> saved = tradingHolidayRepo.saveAll(ths);
            return saved.stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public List<TradingCalendarDTO.Holiday> deleteHolidays(String calendarId,
                                                           List<TradingCalendarDTO.Holiday> holidays) {
        Optional<TradingCalendarDTO> calendar = getCalendar(calendarId);
        if (calendar.isPresent()) {
            return tradingHolidayRepo
                    .deleteByCalendarIdEqualsAndHolidayIn(calendarId,
                            holidays.stream()
                                    .map(TradingCalendarDTO.Holiday::getHoliday)
                                    .collect(Collectors.toList()))
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<CalendarDescriptionDTO> listCalendars() {
        return tradingCalendarRepo.findAllByOrderByCalendarId()
                .stream()
                .map(c -> new CalendarDescriptionDTO(c.getUuid(), c.getCalendarId(), c.getCalendarName()))
                .collect(Collectors.toList());
    }

    @Override
    public void loadIntoQuantlib() {
        List<String> calendars = listCalendars().stream()
                .map(CalendarDescriptionDTO::getCalendarId)
                .collect(Collectors.toList());
        for (String c : calendars) {
            logger.info("Loading calendar " + c);
            getCalendar(c).ifPresent(dto -> Holidays.Instance.replace(dto.getCalendarId(),
                    dto.getHolidays().stream()
                            .map(TradingCalendarDTO.Holiday::getHoliday)
                            .collect(Collectors.toList())));
        }
    }

    @Override
    public String getTradeDateByOffset(int offset, String currentDate){
        List<CalendarDescriptionDTO> calendars = listCalendars();
        if (CollectionUtils.isEmpty(calendars)){
            throw new CustomException("交易日历不存在");
        }
        String calendarId = calendars.get(0).getCalendarId();
        Optional<TradingCalendarDTO> calendar = getCalendar(calendarId);
        if (!calendar.isPresent()) {
            throw new CustomException(ErrorCode.MISSING_ENTITY, "交易日历不存在：" + calendarId);
        }
        List<LocalDate> holidays = calendar.get().getHolidays()
                .stream().map(TradingCalendarDTO.Holiday::getHoliday).collect(Collectors.toList());

        LocalDate curDate = LocalDate.now();
        if (StringUtils.isNotEmpty(currentDate)){
            curDate = LocalDate.parse(currentDate);
        }
        int n = 0;
        int adjustDayNum = offset == 0 ? 0 : ( offset > 0 ? 1 : -1);
        while (true){
            if (offset != 0) {
                curDate = curDate.plusDays(adjustDayNum);
                if (!holidays.contains(curDate)) {
                    n++;
                }
                if (n >= Math.abs(offset)) {
                    break;
                }
            }else {
                if (holidays.contains(curDate)) {
                    curDate = curDate.plusDays(-1);
                }else {
                    break;
                }
            }
        }
        return curDate.toString();
    }
}
