package tech.tongyu.bct.trade.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * LCMNotificationEvent is a snapshot of future notifications regarding to a trade's life cycle events.
 * It only links to the trade and it's position by their natural key and therefore not always respect the position's latest version
 */
@Entity
@Table(schema = TradeService.SCHEMA,
        indexes = {@Index(columnList = "tradeId"), @Index(columnList = "positionId")})
public class LCMNotification implements HasUuid {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    //trade's natural key
    @Column(nullable = false)
    private String tradeId;

    //position's natural key
    @Column(nullable = false)
    private String positionId;

    @Column
    private LocalDateTime notificationTime;

    @Column
    @Enumerated(EnumType.STRING)
    private LCMEventTypeEnum notificationEventType;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode notificationInfo;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public LCMNotification() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTradeId() {
        return tradeId;
    }

    public JsonNode getNotificationInfo() {
        return notificationInfo;
    }

    public void setNotificationInfo(JsonNode notificationInfo) {
        this.notificationInfo = notificationInfo;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

    public LCMEventTypeEnum getNotificationEventType() {
        return notificationEventType;
    }

    public void setNotificationEventType(LCMEventTypeEnum notificationEventType) {
        this.notificationEventType = notificationEventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
