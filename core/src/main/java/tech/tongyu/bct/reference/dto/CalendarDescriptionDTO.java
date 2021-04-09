package tech.tongyu.bct.reference.dto;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.UUID;

public class CalendarDescriptionDTO {
    @BctField(description = "唯一标识符")
    private UUID uuid;
    @BctField(description = "交易日历代码")
    private String calendarId;
    @BctField(description = "交易日历名称")
    private String calendarName;

    public CalendarDescriptionDTO() {
    }

    public CalendarDescriptionDTO(UUID uuid, String calendarId, String calendarName) {
        this.uuid = uuid;
        this.calendarId = calendarId;
        this.calendarName = calendarName;
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
}
