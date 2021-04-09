package tech.tongyu.bct.exchange.dao.dbo.local;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.exchange.dto.OpenCloseEnum;
import tech.tongyu.bct.exchange.service.ExchangeService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = ExchangeService.SCHEMA)
public class TradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String bookId;
    @Column
    private String tradeId;
    @Column
    private String tradeAccount;
    @Column
    private String instrumentId;
    @Column(precision=19,scale=4)
    private BigDecimal multiplier;
    @Column(precision=19,scale=4)
    private BigDecimal dealAmount;
    @Column(precision=19,scale=4)
    private BigDecimal dealPrice;
    @Column
    private LocalDateTime dealTime;

    @Column
    @Enumerated(EnumType.STRING)
    private OpenCloseEnum openClose;
    @Column
    @Enumerated(EnumType.STRING)
    private InstrumentOfValuePartyRoleTypeEnum direction;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    public TradeRecord() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeAccount() {
        return tradeAccount;
    }

    public void setTradeAccount(String tradeAccount) {
        this.tradeAccount = tradeAccount;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public void setDealAmount(BigDecimal dealAmount) {
        this.dealAmount = dealAmount;
    }

    public BigDecimal getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(BigDecimal dealPrice) {
        this.dealPrice = dealPrice;
    }

    public LocalDateTime getDealTime() {
        return dealTime;
    }

    public void setDealTime(LocalDateTime dealTime) {
        this.dealTime = dealTime;
    }

    public OpenCloseEnum getOpenClose() {
        return openClose;
    }

    public void setOpenClose(OpenCloseEnum openClose) {
        this.openClose = openClose;
    }

    public InstrumentOfValuePartyRoleTypeEnum getDirection() {
        return direction;
    }

    public void setDirection(InstrumentOfValuePartyRoleTypeEnum direction) {
        this.direction = direction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
