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
@Table(schema = MarketDataService.SCHEMA,
        indexes = {@Index(columnList = "instrumentId, valuationDate", unique = true)})
public class QuoteClose {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false)
    private String instrumentId;

    @Column(nullable = false)
    private LocalDate valuationDate;

    // close
    @Column
    private Double close;

    @Column
    private Double settle;

    @Column
    private Double open;

    @Column
    private Double low;

    @Column
    private Double high;

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

    public QuoteClose() {
    }

    public QuoteClose(String instrumentId, LocalDate valuationDate,
                      Double close, Double settle, Double open, Double low, Double high,
                      LocalDateTime quoteTimestamp, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.valuationDate = valuationDate;
        this.close = close;
        this.settle = settle;
        this.open = open;
        this.low = low;
        this.high = high;
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

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getSettle() {
        return settle;
    }

    public void setSettle(Double settle) {
        this.settle = settle;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
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
