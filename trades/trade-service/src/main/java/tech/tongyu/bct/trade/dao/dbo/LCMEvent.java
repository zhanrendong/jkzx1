package tech.tongyu.bct.trade.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        schema = TradeService.SNAPSHOT_SCHEMA,
        indexes = {
                @Index(columnList = "tradeId"),
                @Index(columnList = "positionId"),
                @Index(columnList = "eventType"),
                @Index(columnList = "paymentDate")
        })
public class LCMEvent implements HasUuid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String tradeId;

    @Column
    private String positionId;

    @Column
    private String userLoginId;

    @Column(precision = 19, scale = 4)
    private BigDecimal premium;

    @Column(precision = 19, scale = 4)
    private BigDecimal cashFlow;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LCMEventTypeEnum eventType;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode eventDetail;

    @Column
    private LocalDate paymentDate;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public LCMEvent() {
        defaultNumberValue();
    }

    public LCMEvent(LCMEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    // ---------------------Property Generate Get/Set Method-----------------

    public void defaultNumberValue() {
        this.premium = BigDecimal.ZERO;
        this.cashFlow = BigDecimal.ZERO;
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

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getUserLoginId() {
        return userLoginId;
    }

    public void setUserLoginId(String userLoginId) {
        this.userLoginId = userLoginId;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getCashFlow() {
        return cashFlow;
    }

    public void setCashFlow(BigDecimal cashFlow) {
        this.cashFlow = cashFlow;
    }

    public LCMEventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(LCMEventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public JsonNode getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(JsonNode eventDetail) {
        this.eventDetail = eventDetail;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
