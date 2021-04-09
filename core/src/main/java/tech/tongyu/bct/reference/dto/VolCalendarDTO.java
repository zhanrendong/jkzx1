package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class VolCalendarDTO {
    public static class SpecialDate {
        @BctField(description = "唯一标识符")
        private UUID uuid;
        @BctField(description = "特殊日期")
        private LocalDate specialDate;
        @BctField(description = "权重")
        private double weight;
        @BctField(description = "特殊日期备注")
        private String note;

        public SpecialDate() {
        }

        public SpecialDate(UUID uuid, LocalDate specialDate, double weight, String note) {
            this.uuid = uuid;
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
    @BctField(description = "唯一标识符")
    private UUID uuid;
    @BctField(
            description = "波动率日历代码",
            order = 2
    )
    private String calendarId;
    @BctField(
            description = "波动率日历名称",
            order = 3
    )
    private String calendarName;
    @BctField(
            description = "周末vol权重",
            order = 4
    )
    private double weekendWeight;
    @BctField(
            description = "波动率特殊日期",
            isCollection = true,
            componentClass = SpecialDate.class,
            order = 5
    )
    private List<SpecialDate> specialDates;

    public VolCalendarDTO() {
    }

    public VolCalendarDTO(UUID uuid, String calendarId, String calendarName,
                          double weekendWeight, List<SpecialDate> specialDates) {
        this.uuid = uuid;
        this.calendarId = calendarId;
        this.calendarName = calendarName;
        this.weekendWeight = weekendWeight;
        this.specialDates = specialDates;
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

    public List<SpecialDate> getSpecialDates() {
        return specialDates;
    }

    public void setSpecialDates(List<SpecialDate> specialDates) {
        this.specialDates = specialDates;
    }
}
