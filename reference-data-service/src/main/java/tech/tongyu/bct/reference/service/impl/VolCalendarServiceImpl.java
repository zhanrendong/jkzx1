package tech.tongyu.bct.reference.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.quant.library.market.vol.VolCalendarCache;
import tech.tongyu.bct.reference.dao.dbo.VolCalendar;
import tech.tongyu.bct.reference.dao.dbo.VolSpecialDate;
import tech.tongyu.bct.reference.dao.repl.intel.VolCalendarRepo;
import tech.tongyu.bct.reference.dao.repl.intel.VolSpecialDateRepo;
import tech.tongyu.bct.reference.dto.CalendarDescriptionDTO;
import tech.tongyu.bct.reference.dto.VolCalendarDTO;
import tech.tongyu.bct.reference.service.VolCalendarService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VolCalendarServiceImpl implements VolCalendarService {
    private static Logger logger = LoggerFactory.getLogger(VolCalendarServiceImpl.class);

    private VolCalendarRepo volCalendarRepo;
    private VolSpecialDateRepo volSpecialDateRepo;

    @Autowired
    public VolCalendarServiceImpl(VolCalendarRepo volCalendarRepo, VolSpecialDateRepo volSpecialDateRepo) {
        this.volCalendarRepo = volCalendarRepo;
        this.volSpecialDateRepo = volSpecialDateRepo;
    }

    private VolCalendarDTO.SpecialDate convert(VolSpecialDate specialDate) {
        return new VolCalendarDTO.SpecialDate(specialDate.getUuid(), specialDate.getSpecialDate(),
                specialDate.getWeight(), specialDate.getNote());
    }

    private VolSpecialDate convert(String calendarId, VolCalendarDTO.SpecialDate specialDate) {
        return new VolSpecialDate(calendarId, specialDate.getSpecialDate(),
                specialDate.getWeight(), specialDate.getNote());
    }

    private VolCalendarDTO convert(VolCalendar volCalendar, List<VolSpecialDate> volSpecialDates) {
        return new VolCalendarDTO(volCalendar.getUuid(),
                volCalendar.getCalendarId(), volCalendar.getCalendarName(),
                volCalendar.getWeekendWeight(),
                volSpecialDates.stream().map(this::convert).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public VolCalendarDTO create(String calendarId, String calendarName, double weekendWeight,
                                 List<VolCalendarDTO.SpecialDate> specialDates) {

        VolCalendar tradingCalendar = new VolCalendar(calendarId, calendarName, weekendWeight);
        List<VolSpecialDate> volSpecialDates = specialDates.stream()
                .map(h -> new VolSpecialDate(calendarId, h.getSpecialDate(), h.getWeight(), h.getNote()))
                .collect(Collectors.toList());
        VolCalendar savedCalendar = volCalendarRepo.save(tradingCalendar);
        List<VolSpecialDate> savedHolidays = volSpecialDateRepo.saveAll(volSpecialDates);
        return convert(savedCalendar, savedHolidays);
    }

    @Override
    public Optional<VolCalendarDTO> getCalendar(String calendarId) {
        Optional<VolCalendar> tradingCalendar = volCalendarRepo.findByCalendarId(calendarId);
        if (!tradingCalendar.isPresent())
            return Optional.empty();
        List<VolSpecialDate> tradingHolidays = volSpecialDateRepo.findAllByCalendarId(calendarId);
        return Optional.of(convert(tradingCalendar.get(), tradingHolidays));
    }

    @Override
    @Transactional
    public List<VolCalendarDTO.SpecialDate> addSpecialDates(String calendarId,
                                                            List<VolCalendarDTO.SpecialDate> specialDates) {
        Optional<VolCalendarDTO> calendar = getCalendar(calendarId);
        if (calendar.isPresent()) {
            Map<LocalDate, VolCalendarDTO.SpecialDate> input = specialDates.stream()
                    .collect(Collectors.toMap(
                            VolCalendarDTO.SpecialDate::getSpecialDate,
                            i -> i
                    ));
            input.keySet().removeAll(calendar.get().getSpecialDates()
                    .stream()
                    .map(VolCalendarDTO.SpecialDate::getSpecialDate)
                    .collect(Collectors.toList()));
            List<VolSpecialDate> ths = input.values().stream()
                    .map(i -> convert(calendarId, i))
                    .collect(Collectors.toList());
            List<VolSpecialDate> saved = volSpecialDateRepo.saveAll(ths);
            return saved.stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public List<VolCalendarDTO.SpecialDate> deleteSpecialDates(String calendarId,
                                                               List<VolCalendarDTO.SpecialDate> specialDates) {
        Optional<VolCalendarDTO> calendar = getCalendar(calendarId);
        if (calendar.isPresent()) {
            return volSpecialDateRepo
                    .deleteByCalendarIdEqualsAndSpecialDateIn(calendarId,
                            specialDates.stream()
                                    .map(VolCalendarDTO.SpecialDate::getSpecialDate)
                                    .collect(Collectors.toList()))
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public List<VolCalendarDTO.SpecialDate> deleteSpecialDates(List<UUID> specialDateUUIDs) {
        List<VolSpecialDate> entities = volSpecialDateRepo.findAllById(specialDateUUIDs);
        List<VolCalendarDTO.SpecialDate> dates = entities.stream()
                .map(s ->
                        new VolCalendarDTO.SpecialDate(s.getUuid(), s.getSpecialDate(), s.getWeight(), s.getNote()))
                .collect(Collectors.toList());
        volSpecialDateRepo.deleteAll(entities);
        return dates;
    }

    @Override
    @Transactional
    public VolCalendarDTO.SpecialDate updateSpecialDate(UUID specialDateUUID, Double weight, String note) {
        return volSpecialDateRepo.findById(specialDateUUID).map(s -> {
            s.setWeight(weight);
            s.setNote(note);
            volSpecialDateRepo.save(s);
            return new VolCalendarDTO.SpecialDate(s.getUuid(), s.getSpecialDate(), s.getWeight(), s.getNote());
        }).orElseThrow(() -> new CustomException("待删除的特殊日期不存在"));
    }

    @Override
    public List<CalendarDescriptionDTO> listCalendars() {
        return volCalendarRepo.findAllByOrderByCalendarId()
                .stream()
                .map(c -> new CalendarDescriptionDTO(c.getUuid(), c.getCalendarId(), c.getCalendarName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VolCalendarDTO updateWeekendWeight(String calendarId, double weekendWeight) {
        return volCalendarRepo.findByCalendarId(calendarId).map(c -> {
            c.setWeekendWeight(weekendWeight);
            volCalendarRepo.save(c);
            return getCalendar(calendarId).get();
        }).orElseThrow(() -> new CustomException(String.format("波动率日历%s不存在", calendarId)));
    }

    @Override
    public void loadIntoQuantlib() {
        List<String> calendars = listCalendars().stream()
                .map(CalendarDescriptionDTO::getCalendarId)
                .collect(Collectors.toList());
        for (String c : calendars) {
            logger.info("Loading Vol Calendar " + c);
            getCalendar(c).ifPresent(dto -> VolCalendarCache.create(dto.getCalendarId(),
                    dto.getWeekendWeight(),
                    dto.getSpecialDates().stream()
                            .collect(Collectors.toMap(VolCalendarDTO.SpecialDate::getSpecialDate,
                                    VolCalendarDTO.SpecialDate::getWeight))));
        }
    }
}
