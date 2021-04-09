package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TradingCalendarDTO {
    public static class Holiday {
        @BctField(description = "唯一标识符")
        private UUID uuid;
        @BctField(description = "节日日期")
        private LocalDate holiday;
        @BctField(description = "日期备注")
        private String note;

        public Holiday() {
        }

        public Holiday(LocalDate holiday, String note) {
            this.uuid = null;
            this.holiday = holiday;
            this.note = note;
        }

        public Holiday(UUID uuid, LocalDate holiday, String note) {
            this.uuid = uuid;
            this.holiday = holiday;
            this.note = note;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
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

    @BctField(description = "唯一标识符")
    private UUID uuid;
    @BctField(
            description = "交易日历代码",
            order = 2
    )
    private String calendarId;
    @BctField(
            description = "交易日历名称",
            order = 3
    )
    private String calendarName;
    @BctField(
            description = "非交易日列表",
            isCollection = true,
            componentClass = Holiday.class,
            order = 4
    )
    private List<Holiday> holidays;

    public TradingCalendarDTO() {
    }

    public TradingCalendarDTO(UUID uuid, String calendarId, String calendarName, List<Holiday> holidays) {
        this.uuid = uuid;
        this.calendarId = calendarId;
        this.calendarName = calendarName;
        this.holidays = holidays;
    }

    public TradingCalendarDTO(String calendarId, String calendarName, List<Holiday> holidays) {
        this.uuid = null;
        this.calendarId = calendarId;
        this.calendarName = calendarName;
        this.holidays = holidays;
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

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<Holiday> holidays) {
        this.holidays = holidays;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
