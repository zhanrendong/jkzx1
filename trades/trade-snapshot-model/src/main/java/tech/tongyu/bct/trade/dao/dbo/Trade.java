package tech.tongyu.bct.trade.dao.dbo;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.common.jpa.JsonConverter;
import tech.tongyu.bct.cm.trade.TradeStatusEnum;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SNAPSHOT_SCHEMA,
        indexes = {@Index(name = "TRADE_INDEX", columnList = "tradeId", unique = true)})
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String bookName;

    @Column
    private String tradeId;

    @Column
    private String trader;

    @Column
    private String comment;

    @Column(length = 1000)
    private String positions;

    @Column
    @ElementCollection
    private Set<String> positionIds;

    @Column
    @Enumerated(EnumType.STRING)
    private TradeStatusEnum tradeStatus;

    @Convert(converter = JsonConverter.class)
    @Column(nullable = false, length = JsonConverter.LENGTH)
    private JsonNode nonEconomicPartyRoles;

    @Column
    private LocalDate tradeDate;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Column
    @UpdateTimestamp
    private Instant updatedAt;


    public Trade(){}

    // ---------------------Property Generate Get/Set Method-----------------


    public Set<String> getPositionIds() {
        return positionIds;
    }

    public void setPositionIds(Set<String> positionIds) {
        this.positionIds = positionIds;
    }

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

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public TradeStatusEnum getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(TradeStatusEnum tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public JsonNode getNonEconomicPartyRoles() {
        return nonEconomicPartyRoles;
    }

    public void setNonEconomicPartyRoles(JsonNode nonEconomicPartyRoles) {
        this.nonEconomicPartyRoles = nonEconomicPartyRoles;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
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
