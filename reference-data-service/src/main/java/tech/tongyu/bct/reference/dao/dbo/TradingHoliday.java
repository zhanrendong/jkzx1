package tech.tongyu.bct.reference.dao.dbo;

import tech.tongyu.bct.reference.service.TradingCalendarService;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = TradingCalendarService.SCHEMA, indexes = {@Index(columnList = "calendarId, holiday", unique = true)})
public class TradingHoliday {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String calendarId;

    @Column(nullable = false)
    private LocalDate holiday;

    @Column
    private String note;

    public TradingHoliday() {
    }

    public TradingHoliday(String calendarId, LocalDate holiday, String note) {
        this.calendarId = calendarId;
        this.holiday = holiday;
        this.note = note;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    public LocalDate getHoliday() {
        return holiday;
    }

    public void setHoliday(LocalDate holiday) {
        this.holiday = holiday;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
