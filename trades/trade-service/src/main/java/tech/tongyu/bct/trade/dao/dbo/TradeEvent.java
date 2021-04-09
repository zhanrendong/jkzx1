package tech.tongyu.bct.trade.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.trade.dto.TradeEventTypeEnum;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SNAPSHOT_SCHEMA)
public class TradeEvent implements HasUuid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    private String tradeId;

    @Column
    private String userLoginId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TradeEventTypeEnum eventType;

    @Column
    @CreationTimestamp
    private Instant createdAt;


    public TradeEvent() {}

    // ---------------------Property Generate Get/Set Method-----------------

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public TradeEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(TradeEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
