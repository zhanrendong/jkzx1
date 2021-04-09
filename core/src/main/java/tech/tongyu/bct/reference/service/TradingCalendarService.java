package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.TradingCalendarDTO;
import tech.tongyu.bct.reference.dto.CalendarDescriptionDTO;

import java.util.List;
import java.util.Optional;

public interface TradingCalendarService {
    String SCHEMA = "referenceDataService";

    TradingCalendarDTO create(String calendarId, String calendarName, List<TradingCalendarDTO.Holiday> holidays);
    Optional<TradingCalendarDTO> getCalendar(String calendarId);
    List<TradingCalendarDTO.Holiday> addHolidays(String calendarId, List<TradingCalendarDTO.Holiday> holidays);
    List<TradingCalendarDTO.Holiday> deleteHolidays(String calendarId, List<TradingCalendarDTO.Holiday> holidays);

    List<CalendarDescriptionDTO> listCalendars();

    void loadIntoQuantlib();

    String getTradeDateByOffset(int offset, String currentDate);
}
