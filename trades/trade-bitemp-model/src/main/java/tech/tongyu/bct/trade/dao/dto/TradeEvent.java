package tech.tongyu.bct.trade.dao.dto;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.trade.dto.TradeEventTypeEnum;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "public")
public class TradeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID uuid;

    @Column(nullable = false)
    UUID tradeVersionUUID;

    @Column(nullable = false)
    TradeEventTypeEnum eventType;

    @Column(nullable = false)
    String userLoginId;

    @CreationTimestamp
    @Column
    Instant createdAt;

    public TradeEvent() {
    }

    public TradeEvent(UUID tradeVersionUUID, TradeEventTypeEnum eventType, String userLoginId) {
        this.tradeVersionUUID = tradeVersionUUID;
        this.eventType = eventType;
        this.userLoginId = userLoginId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getTradeVersionUUID() {
        return tradeVersionUUID;
    }

    public void setTradeVersionUUID(UUID tradeVersionUUID) {
        this.tradeVersionUUID = tradeVersionUUID;
    }

    public TradeEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(TradeEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
