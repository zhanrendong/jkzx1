package tech.tongyu.bct.reference.dao.dbo;

import tech.tongyu.bct.reference.service.TradingCalendarService;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = TradingCalendarService.SCHEMA)
public class VolCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String calendarId;

    @Column(nullable = false)
    private String calendarName;

    @Column(nullable = false)
    private double weekendWeight;

    public VolCalendar() {
    }

    public VolCalendar(String calendarId, String calendarName, double weekendWeight) {
        this.calendarId = calendarId;
        this.calendarName = calendarName;
        this.weekendWeight = weekendWeight;
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

    public String getCalendarName() {
        return calendarName;
    }

    public void setCalendarName(String calendarName) {
        this.calendarName = calendarName;
    }

    public double getWeekendWeight() {
        return weekendWeight;
    }

    public void setWeekendWeight(double weekendWeight) {
        this.weekendWeight = weekendWeight;
    }
}
