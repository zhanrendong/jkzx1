package tech.tongyu.bct.market.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.market.service.MarketDataService;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(schema = MarketDataService.SCHEMA, indexes = {@Index(columnList = "instrumentId", unique = true)})
public class QuoteIntraday {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String instrumentId;

    @Column(nullable = false)
    private LocalDate valuationDate;

    // close
    @Column
    private Double last;

    @Column
    private Double bid;

    @Column
    private Double ask;

    @Column
    private Double open;

    @Column
    private Double yesterdayClose;

    @Column(nullable = false)
    private LocalDateTime quoteTimestamp;

    @Column(nullable = false)
    private ZoneId quoteTimezone;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public QuoteIntraday() {
    }

    public QuoteIntraday(String instrumentId, LocalDate valuationDate,
                         Double last, Double bid, Double ask, Double open, Double yesterdayClose,
                         LocalDateTime quoteTimestamp, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.valuationDate = valuationDate;
        this.last = last;
        this.bid = bid;
        this.ask = ask;
        this.open = open;
        this.yesterdayClose = yesterdayClose;
        this.quoteTimestamp = quoteTimestamp;
        this.quoteTimezone = quoteTimezone;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public Double getLast() {
        return last;
    }

    public void setLast(Double last) {
        this.last = last;
    }

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getYesterdayClose() {
        return yesterdayClose;
    }

    public void setYesterdayClose(Double yesterdayClose) {
        this.yesterdayClose = yesterdayClose;
    }

    public LocalDateTime getQuoteTimestamp() {
        return quoteTimestamp;
    }

    public void setQuoteTimestamp(LocalDateTime quoteTimestamp) {
        this.quoteTimestamp = quoteTimestamp;
    }

    public ZoneId getQuoteTimezone() {
        return quoteTimezone;
    }

    public void setQuoteTimezone(ZoneId quoteTimezone) {
        this.quoteTimezone = quoteTimezone;
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
