package tech.tongyu.bct.trade.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SNAPSHOT_SCHEMA,
        indexes = {@Index(name = "POSITION_INDEX", columnList = "positionId", unique = true)})
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String bookName;

    @Column
    private String positionId;

    @Column(precision=19,scale=4)
    private BigDecimal quantity;

    @Column
    @Enumerated(EnumType.STRING)
    private LCMEventTypeEnum lcmEventType;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode asset;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode counterparty;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode positionAccount;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode counterpartyAccount;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;

    public Position(){}

    // ---------------------Property Generate Get/Set Method-----------------

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public LCMEventTypeEnum getLcmEventType() {
        return lcmEventType;
    }

    public void setLcmEventType(LCMEventTypeEnum lcmEventType) {
        this.lcmEventType = lcmEventType;
    }

    public JsonNode getAsset() {
        return asset;
    }

    public void setAsset(JsonNode asset) {
        this.asset = asset;
    }

    public JsonNode getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(JsonNode counterparty) {
        this.counterparty = counterparty;
    }

    public JsonNode getPositionAccount() {
        return positionAccount;
    }

    public void setPositionAccount(JsonNode positionAccount) {
        this.positionAccount = positionAccount;
    }

    public JsonNode getCounterpartyAccount() {
        return counterpartyAccount;
    }

    public void setCounterpartyAccount(JsonNode counterpartyAccount) {
        this.counterpartyAccount = counterpartyAccount;
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
