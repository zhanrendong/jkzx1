package tech.tongyu.bct.exchange.dao.dbo.local;

import org.hibernate.annotations.CreationTimestamp;
import tech.tongyu.bct.exchange.service.ExchangeService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(schema = ExchangeService.SCHEMA)
public class PositionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column
    private String bookId;

    @Column
    private String instrumentId;

    @Column(precision=19,scale=4)
    private BigDecimal longPosition;

    @Column(precision=19,scale=4)
    private BigDecimal shortPosition;

    @Column(precision=19,scale=4)
    private BigDecimal netPosition;

    @Column(precision=19,scale=4)
    private BigDecimal totalSell;

    @Column(precision=19,scale=4)
    private BigDecimal totalBuy;

    @Column(precision=19,scale=4)
    private BigDecimal historyBuyAmount;

    @Column(precision=19,scale=4)
    private BigDecimal historySellAmount;

    @Column(precision=19,scale=4)
    private BigDecimal marketValue;

    @Column(precision=19,scale=4)
    private BigDecimal totalPnl;

    @Column
    private LocalDate dealDate;

    @Column
    private LocalTime dealTime;

    @Column
    @CreationTimestamp
    private Instant createdAt;

    public PositionRecord() {
        this.longPosition = BigDecimal.ZERO;
        this.shortPosition = BigDecimal.ZERO;
        this.netPosition = BigDecimal.ZERO;
        this.historyBuyAmount = BigDecimal.ZERO;
        this.historySellAmount = BigDecimal.ZERO;
        this.marketValue = BigDecimal.ZERO;
        this.totalPnl = BigDecimal.ZERO;
        this.totalBuy = BigDecimal.ZERO;
        this.totalSell = BigDecimal.ZERO;
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

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public BigDecimal getLongPosition() {
        return longPosition;
    }

    public void setLongPosition(BigDecimal longPosition) {
        this.longPosition = longPosition;
    }

    public BigDecimal getShortPosition() {
        return shortPosition;
    }

    public void setShortPosition(BigDecimal shortPosition) {
        this.shortPosition = shortPosition;
    }

    public BigDecimal getNetPosition() {
        return netPosition;
    }

    public void setNetPosition(BigDecimal netPosition) {
        this.netPosition = netPosition;
    }

    public BigDecimal getTotalSell() {
        return totalSell;
    }

    public void setTotalSell(BigDecimal totalSell) {
        this.totalSell = totalSell;
    }

    public BigDecimal getTotalBuy() {
        return totalBuy;
    }

    public void setTotalBuy(BigDecimal totalBuy) {
        this.totalBuy = totalBuy;
    }

    public BigDecimal getHistoryBuyAmount() {
        return historyBuyAmount;
    }

    public void setHistoryBuyAmount(BigDecimal historyBuyAmount) {
        this.historyBuyAmount = historyBuyAmount;
    }

    public BigDecimal getHistorySellAmount() {
        return historySellAmount;
    }

    public void setHistorySellAmount(BigDecimal historySellAmount) {
        this.historySellAmount = historySellAmount;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getTotalPnl() {
        return totalPnl;
    }

    public void setTotalPnl(BigDecimal totalPnl) {
        this.totalPnl = totalPnl;
    }

    public LocalDate getDealDate() {
        return dealDate;
    }

    public void setDealDate(LocalDate dealDate) {
        this.dealDate = dealDate;
    }

    public LocalTime getDealTime() {
        return dealTime;
    }

    public void setDealTime(LocalTime dealTime) {
        this.dealTime = dealTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
