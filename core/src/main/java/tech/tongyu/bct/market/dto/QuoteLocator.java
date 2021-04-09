package tech.tongyu.bct.market.dto;

import tech.tongyu.bct.common.Locator;
import tech.tongyu.bct.common.util.DateTimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class QuoteLocator implements Locator {
    private String instrumentId;
    private InstanceEnum instance;
    private LocalDate valuationDate;
    private LocalDateTime quoteTimestamp;
    private ZoneId quoteTimezone;

    public QuoteLocator() {
    }

    public QuoteLocator(String instrumentId, InstanceEnum instance,
                        LocalDate valuationDate, LocalDateTime quoteTimestamp, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.quoteTimestamp = quoteTimestamp;
        this.quoteTimezone = quoteTimezone;
    }

    public QuoteLocator(String instrumentId, InstanceEnum instance, LocalDate valuationDate, ZoneId quoteTimezone) {
        this.instrumentId = instrumentId;
        this.instance = instance;
        this.valuationDate = valuationDate;
        this.quoteTimestamp = DateTimeUtils.BCT_MAX_LOCALDATETIME;
        this.quoteTimezone = quoteTimezone;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public InstanceEnum getInstance() {
        return instance;
    }

    public void setInstance(InstanceEnum instance) {
        this.instance = instance;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
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

    @Override
    public String toString() {
        return "QuoteLocator{" +
                "instrumentId='" + instrumentId + '\'' +
                ", instance=" + instance +
                ", valuationDate=" + valuationDate +
                ", quoteTimestamp=" + quoteTimestamp +
                ", quoteTimezone=" + quoteTimezone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuoteLocator)) return false;
        QuoteLocator that = (QuoteLocator) o;
        return Objects.equals(getInstrumentId(), that.getInstrumentId()) &&
                getInstance() == that.getInstance() &&
                Objects.equals(getValuationDate(), that.getValuationDate()) &&
                Objects.equals(getQuoteTimestamp(), that.getQuoteTimestamp()) &&
                Objects.equals(getQuoteTimezone(), that.getQuoteTimezone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstrumentId(), getInstance(),
                getValuationDate(), getQuoteTimestamp(), getQuoteTimezone());
    }
}
