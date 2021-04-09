package tech.tongyu.bct.trade.dto.lcm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.api.doc.BctField;

import java.time.LocalDateTime;


public class LCMNotificationDTO {
    public static final String eventInfoFieldName = "eventInfo";
    @BctField(name = "notificationUUID", description = "生命周期事件通知唯一标识", type = "String")
    public String notificationUUID;
    @BctField(name = "tradeId", description = "交易ID", type = "String")
    public String tradeId;
    @BctField(name = "positionId", description = "持仓ID", type = "String")
    public String positionId;
    @BctField(name = "notificationTime", description = "通知时间", type = "LocalDateTime")
    public LocalDateTime notificationTime;
    @BctField(name = "notificationEventType", description = "生命周期事件类型", type = "LCMEventTypeEnum")
    public LCMEventTypeEnum notificationEventType;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonProperty(eventInfoFieldName)
    public LCMNotificationInfoDTO eventInfo;

    public LCMNotificationDTO() {
    }

    public LCMNotificationDTO(String notificationUUID, String tradeId, String positionId, LocalDateTime notificationTime,
                              LCMEventTypeEnum notificationEventType, LCMNotificationInfoDTO eventInfo) {
        this.notificationUUID = notificationUUID;
        this.tradeId = tradeId;
        this.positionId = positionId;
        this.notificationTime = notificationTime;
        this.notificationEventType = notificationEventType;
        this.eventInfo = eventInfo;
    }
}
