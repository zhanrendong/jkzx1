package tech.tongyu.bct.reference.dao.dbo;

import tech.tongyu.bct.reference.service.TradingCalendarService;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = TradingCalendarService.SCHEMA,
        indexes = {@Index(columnList = "calendarId, specialDate", unique = true)})
public class VolSpecialDate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String calendarId;

    @Column(nullable = false)
    private LocalDate specialDate;

    @Column(nullable = false)
    private double weight;

    @Column
    private String note;

    public VolSpecialDate() {
    }

    public VolSpecialDate(String calendarId, LocalDate specialDate, double weight, String note) {
        this.calendarId = calendarId;
        this.specialDate = specialDate;
        this.weight = weight;
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

    public LocalDate getSpecialDate() {
        return specialDate;
    }

    public void setSpecialDate(LocalDate specialDate) {
        this.specialDate = specialDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
