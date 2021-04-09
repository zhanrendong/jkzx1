package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.CalendarDescriptionDTO;
import tech.tongyu.bct.reference.dto.VolCalendarDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VolCalendarService {
    String SCHEMA = "referenceDataService";

    VolCalendarDTO create(String calendarId, String calendarName,
                          double weekendWeight, List<VolCalendarDTO.SpecialDate> specialDates);
    Optional<VolCalendarDTO> getCalendar(String calendarId);
    List<VolCalendarDTO.SpecialDate> addSpecialDates(String calendarId, List<VolCalendarDTO.SpecialDate> holidays);
    List<VolCalendarDTO.SpecialDate> deleteSpecialDates(String calendarId, List<VolCalendarDTO.SpecialDate> holidays);
    List<VolCalendarDTO.SpecialDate> deleteSpecialDates(List<UUID> specialDateUUIDs);
    VolCalendarDTO.SpecialDate updateSpecialDate(UUID specialDateUUID, Double weight, String note);
    List<CalendarDescriptionDTO> listCalendars();
    VolCalendarDTO updateWeekendWeight(String calendarId, double weekendWeight);

    void loadIntoQuantlib();
}
